package no.nav.dagpenger.inntekt.subsumsjonbrukt

import de.huxhorn.sulky.ulid.ULID
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.dagpenger.events.moshiInstance
import no.nav.dagpenger.inntekt.Config
import no.nav.dagpenger.inntekt.Config.inntektApiConfig
import no.nav.dagpenger.inntekt.HealthStatus
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.plain.producerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Test
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.utility.DockerImageName
import java.sql.SQLTransientConnectionException
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

private val LOGGER = KotlinLogging.logger { }

internal class KafkaSubsumsjonBruktDataConsumerTest {
    private object Kafka {
        val instance by lazy {
            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka").withTag("7.5.1")).apply {
                this.waitingFor(HostPortWaitStrategy())
                this.start()
            }
        }
    }

    val inntektId = InntektId(ULID().nextULID())
    private val bruktInntektMelding =
        mapOf(
            "@event_name" to "brukt_inntekt",
            "inntektsId" to inntektId.id,
            "aktorId" to "12345678910",
            "kontekst" to
                mapOf(
                    "id" to "2",
                    "type" to "vedtak",
                ),
        )
    private val bruktInntektMeldingManueltGrunnlag =
        mapOf(
            "@event_name" to "brukt_inntekt",
            "aktorId" to "12345678910",
            "kontekst" to
                mapOf(
                    "id" to "1",
                    "type" to "vedtak",
                ),
        )
    private val producer by lazy {
        KafkaProducer<String, String>(
            producerConfig(
                clientId = "test",
                bootstrapServers = Kafka.instance.bootstrapServers,
            ).also {
                it[ProducerConfig.ACKS_CONFIG] = "all"
                it[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
            },
        )
    }
    private val adapter = moshiInstance.adapter<Map<String, Any?>>(Map::class.java).lenient()

    @Test
    fun `Should mark inntekt id as used`() {
        runBlocking {
            val storeMock = mockk<InntektStore>(relaxed = false)
            coEvery { storeMock.markerInntektBrukt(inntektId) } returns 1
            val config =
                Config.config.inntektApiConfig.run {
                    copy(application = application.copy(brokers = Kafka.instance.bootstrapServers, credential = null))
                }
            val consumer =
                KafkaSubsumsjonBruktDataConsumer(config, storeMock).apply {
                    listen()
                }
            val metaData =
                producer.send(ProducerRecord(config.inntektBruktDataTopic, "test", adapter.toJson(bruktInntektMelding)))
                    .get(5, TimeUnit.SECONDS)
            LOGGER.info("Producer produced $bruktInntektMelding with meta $metaData")

            TimeUnit.MILLISECONDS.sleep(800)

            verify(exactly = 1) {
                storeMock.markerInntektBrukt(inntektId)
            }

            consumer.stop()
        }
    }

    @Test
    fun `Cannot mark inntekt id as used if not present in faktum`() {
        runBlocking {
            val storeMock = mockk<InntektStore>(relaxed = false)
            val config =
                Config.config.inntektApiConfig.run {
                    copy(application = application.copy(brokers = Kafka.instance.bootstrapServers, credential = null))
                }
            val consumer =
                KafkaSubsumsjonBruktDataConsumer(config, storeMock).apply {
                    listen()
                }
            val bruktSubsumsjonData = mapOf("faktum" to mapOf("manueltGrunnlag" to "122212"))
            val metaData =
                producer.send(
                    ProducerRecord(
                        config.inntektBruktDataTopic,
                        "test",
                        adapter.toJson(bruktInntektMeldingManueltGrunnlag),
                    ),
                )
                    .get(5, TimeUnit.SECONDS)
            LOGGER.info("Producer produced $bruktSubsumsjonData with meta $metaData")

            TimeUnit.MILLISECONDS.sleep(500)

            verify(exactly = 0) {
                storeMock.markerInntektBrukt(any())
            }

            consumer.status() shouldBe HealthStatus.UP
            consumer.stop()
        }
    }

    @Test
    fun `Should have grace period on health status when job is no longer active`() {
        runBlocking {
            val inntektId = InntektId(ULID().nextULID())
            val storeMock = mockk<InntektStore>(relaxed = false)
            coEvery { storeMock.markerInntektBrukt(inntektId) } throws SQLTransientConnectionException("BLÆ")
            val config =
                Config.config.inntektApiConfig.run {
                    copy(application = application.copy(brokers = Kafka.instance.bootstrapServers, credential = null))
                }
            val consumer =
                KafkaSubsumsjonBruktDataConsumer(
                    config = config,
                    inntektStore = storeMock,
                    graceDuration = Duration.ofMillis(1),
                ).apply {
                    listen()
                }
            val metaData =
                producer.send(ProducerRecord(config.inntektBruktDataTopic, "test", adapter.toJson(bruktInntektMelding)))
                    .get(5, TimeUnit.SECONDS)
            LOGGER.info("Producer produced $bruktInntektMelding with meta $metaData + should fail")

            TimeUnit.MILLISECONDS.sleep(1500)

            consumer.status() shouldBe HealthStatus.DOWN
        }
    }

    @Test
    fun `Grace period is over`() {
        val graceperiod1 =
            KafkaSubsumsjonBruktDataConsumer.Grace(from = ZonedDateTime.now(ZoneOffset.UTC).minusHours(1))
        graceperiod1.expired() shouldBe false
        val graceperiod2 =
            KafkaSubsumsjonBruktDataConsumer.Grace(from = ZonedDateTime.now(ZoneOffset.UTC).minusHours(4))
        graceperiod2.expired() shouldBe true
    }
}
