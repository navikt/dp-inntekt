package no.nav.dagpenger.inntekt.subsumsjonbrukt

import com.github.navikt.tbd_libs.kafka.ConsumerProducerFactory
import de.huxhorn.sulky.ulid.ULID
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.inntekt.Config
import no.nav.dagpenger.inntekt.Config.inntektApiConfig
import no.nav.dagpenger.inntekt.HealthStatus
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.serder.inntektObjectMapper
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Test
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.utility.DockerImageName
import java.sql.SQLTransientConnectionException
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Properties
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

private val LOGGER = KotlinLogging.logger { }

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
internal class KafkaSubsumsjonBruktDataConsumerTest {
    private object Kafka {
        val instance by lazy {
            ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka").withTag("7.5.1")).apply {
                this.waitingFor(HostPortWaitStrategy())
                this.start()
            }
        }
    }

    private val consumerProducerFactory by lazy {
        ConsumerProducerFactory(
            LocalKafkaConfig(
                mapOf(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to Kafka.instance.bootstrapServers).toProperties(),
            ),
        )
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
        consumerProducerFactory.createProducer()
    }

    private val consumer by lazy {
        consumerProducerFactory.createConsumer("test-group")
    }

    @Test
    fun `Should mark inntekt id as used`() =
        runBlocking {
            val storeMock = mockk<InntektStore>(relaxed = false)
            coEvery { storeMock.markerInntektBrukt(inntektId) } returns 1
            val topic = Config.config.inntektApiConfig.inntektBruktDataTopic
            val consumer =
                KafkaSubsumsjonBruktDataConsumer(
                    consumer,
                    topic,
                    storeMock,
                )

            newSingleThreadContext("test-1").use {
                launch(it) {
                    consumer.listen()
                }
            }

            val metaData =
                producer
                    .send(
                        ProducerRecord(
                            topic,
                            "test",
                            inntektObjectMapper.writeValueAsString(bruktInntektMelding),
                        ),
                    ).get(5, TimeUnit.SECONDS)
            LOGGER.info { "Producer produced $bruktInntektMelding with meta $metaData" }

            delay(500)
            verify(exactly = 1) {
                storeMock.markerInntektBrukt(inntektId)
            }

            consumer.stop()
        }

    @Test
    fun `Cannot mark inntekt id as used if not present in faktum`() =
        runBlocking {
            val storeMock = mockk<InntektStore>(relaxed = false)

            val topic = Config.config.inntektApiConfig.inntektBruktDataTopic
            val consumer =
                KafkaSubsumsjonBruktDataConsumer(
                    consumer,
                    topic,
                    storeMock,
                )

            newSingleThreadContext("test-2").use {
                launch(it) {
                    consumer.listen()
                }
            }

            val bruktSubsumsjonData = mapOf("faktum" to mapOf("manueltGrunnlag" to "122212"))
            val metaData =
                producer
                    .send(
                        ProducerRecord(
                            topic,
                            "test",
                            inntektObjectMapper.writeValueAsString(bruktInntektMeldingManueltGrunnlag),
                        ),
                    ).get(5, TimeUnit.SECONDS)
            LOGGER.info { "Producer produced $bruktSubsumsjonData with meta $metaData" }

            delay(500)

            verify(exactly = 0) {
                storeMock.markerInntektBrukt(any())
            }

            consumer.status() shouldBe HealthStatus.UP
            consumer.stop()
        }

    @Test
    fun `Should have grace period on health status when job is no longer active`(): Unit =
        runBlocking {
            val inntektId = InntektId(ULID().nextULID())
            val storeMock = mockk<InntektStore>(relaxed = false)
            coEvery { storeMock.markerInntektBrukt(inntektId) } throws SQLTransientConnectionException("BLÆ")
            val topic = Config.config.inntektApiConfig.inntektBruktDataTopic
            val consumer =
                KafkaSubsumsjonBruktDataConsumer(
                    consumer,
                    topic,
                    storeMock,
                    graceDuration = 1.milliseconds.toJavaDuration(),
                )

            newSingleThreadContext("test-3").use {
                launch(it) {
                    consumer.listen()
                }
            }

            val metaData =
                producer
                    .send(
                        ProducerRecord(
                            topic,
                            "test",
                            inntektObjectMapper.writeValueAsString(bruktInntektMelding),
                        ),
                    ).get(5, TimeUnit.SECONDS)
            LOGGER.info { "Producer produced $bruktInntektMelding with meta $metaData + should fail" }

            TimeUnit.MILLISECONDS.sleep(1500)

            consumer.status() shouldBe HealthStatus.DOWN
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

    internal class LocalKafkaConfig(private val connectionProperties: Properties) :
        com.github.navikt.tbd_libs.kafka.Config {
        override fun producerConfig(properties: Properties) =
            properties.apply {
                putAll(connectionProperties)
                put(ProducerConfig.ACKS_CONFIG, "all")
                put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1")
                put(ProducerConfig.LINGER_MS_CONFIG, "0")
                put(ProducerConfig.RETRIES_CONFIG, "0")
            }

        override fun consumerConfig(
            groupId: String,
            properties: Properties,
        ) = properties.apply {
            putAll(connectionProperties)
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        }

        override fun adminConfig(properties: Properties) =
            properties.apply {
                putAll(connectionProperties)
            }
    }
}
