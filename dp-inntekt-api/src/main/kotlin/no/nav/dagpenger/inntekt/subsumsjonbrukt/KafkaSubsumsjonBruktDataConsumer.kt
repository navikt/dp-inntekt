package no.nav.dagpenger.inntekt.subsumsjonbrukt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.inntekt.HealthCheck
import no.nav.dagpenger.inntekt.HealthStatus
import no.nav.dagpenger.inntekt.InntektApiConfig
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.plain.consumerConfig
import no.nav.dagpenger.streams.PacketDeserializer
import org.apache.kafka.clients.consumer.CommitFailedException
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.coroutines.CoroutineContext

internal class KafkaSubsumsjonBruktDataConsumer(
    private val config: InntektApiConfig,
    private val inntektStore: InntektStore,
    private val graceDuration: Duration = Duration.ofHours(3),
) : CoroutineScope, HealthCheck {
    private val logger = KotlinLogging.logger { }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val grace by lazy {
        Grace(duration = graceDuration)
    }

    private val job: Job by lazy {
        SupervisorJob()
    }

    fun listen() {
        launch(coroutineContext) {
            logger.info { "Starting ${config.application.id}" }

            KafkaConsumer<String, Packet>(
                consumerConfig(
                    groupId = config.application.id,
                    bootstrapServerUrl = config.application.brokers,
                    credential = config.application.credential,
                ).also {
                    it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = PacketDeserializer::class.java
                    it[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
                    it[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false"
                    it[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 10
                },
            ).use { consumer ->
                try {
                    consumer.subscribe(listOf(config.inntektBruktDataTopic))
                    while (job.isActive) {
                        val records = consumer.poll(Duration.ofMillis(100))
                        val ids =
                            records.asSequence()
                                .map { record -> record.value() }
                                .filter { packet ->
                                    packet.hasFields(
                                        "@event_name",
                                        "aktorId",
                                        "inntektsId",
                                        "kontekst",
                                    ) &&
                                        packet.getStringValue("@event_name") == "brukt_inntekt"
                                }
                                .map { packet -> InntektId(packet.getStringValue("inntektsId")) }
                                .toList()

                        try {
                            ids.forEach { id ->
                                if (inntektStore.markerInntektBrukt(id) == 1) {
                                    logger.info("Marked inntekt with id $id as used")
                                }
                            }
                            if (ids.isNotEmpty()) {
                                consumer.commitSync()
                            }
                        } catch (e: CommitFailedException) {
                            logger.warn("Kafka threw a commit fail exception, looping back", e)
                        }
                    }
                } catch (e: Exception) {
                    logger.error(
                        """
                        "Unexpected exception while consuming messages. 
                         Stopping consumer, grace period ${grace.duration.seconds / 60} minutes"
                        """.trimIndent(),
                        e,
                    )
                    stop()
                }
            }
        }
    }

    override fun status(): HealthStatus {
        return if (job.isActive) {
            HealthStatus.UP
        } else {
            return if (grace.expired()) {
                HealthStatus.DOWN
            } else {
                HealthStatus.UP
            }
        }
    }

    fun stop() {
        logger.info { "Stopping ${config.application.id} consumer" }
        job.cancel()
    }

    data class Grace(
        val duration: Duration = Duration.ofHours(3),
        val from: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    ) {
        private val expires = from.plus(duration)

        fun expired() = ZonedDateTime.now(ZoneOffset.UTC).isAfter(expires)
    }
}
