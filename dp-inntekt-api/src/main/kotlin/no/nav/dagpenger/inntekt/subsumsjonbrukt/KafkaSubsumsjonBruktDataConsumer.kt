package no.nav.dagpenger.inntekt.subsumsjonbrukt

import com.github.navikt.tbd_libs.kafka.AivenConfig
import com.github.navikt.tbd_libs.kafka.ConsumerProducerFactory
import com.github.navikt.tbd_libs.kafka.poll
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.dagpenger.inntekt.HealthCheck
import no.nav.dagpenger.inntekt.HealthStatus
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.serder.inntektObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean

internal class KafkaSubsumsjonBruktDataConsumer(
    private val kafkaConsumer: KafkaConsumer<String, String>,
    private val topic: String,
    private val inntektStore: InntektStore,
    private val graceDuration: Duration = Duration.ofHours(3),
) : HealthCheck {
    private val logger = KotlinLogging.logger { }

    private val running = AtomicBoolean(false)
    private var grace: Grace? = null

    private fun startGracePeriod() {
        if (grace == null) {
            grace = Grace(duration = graceDuration)
            logger.warn { "Grace period started, expires in ${graceDuration.seconds / 60} minutes" }
        }
    }

    fun listen() {
        running.set(true)
        kafkaConsumer.use { consumer ->
            consumer.subscribe(listOf(topic))
            logger.info { "Start consuming $topic with consumer ${kafkaConsumer.groupMetadata().groupId()}" }
            try {
                consumer.poll(running::get) { records ->
                    val ids =
                        records
                            .asSequence()
                            .map { record -> record.value() }
                            .map { inntektObjectMapper.readTree(it) }
                            .filter { packet ->
                                packet.has("@event_name") &&
                                    packet.has("aktorId") &&
                                    packet.has("inntektsId") &&
                                    packet.has("kontekst") &&
                                    packet.get("@event_name").asText() == "brukt_inntekt"
                            }.map { packet -> InntektId(packet.get("inntektsId").asText()) }
                            .toList()
                    ids.forEach { id ->
                        if (inntektStore.markerInntektBrukt(id) == 1) {
                            logger.info { "Marked inntekt with id $id as used" }
                        }
                    }
                    if (ids.isNotEmpty()) {
                        consumer.commitSync()
                    }
                }
            } catch (err: WakeupException) {
                logger.info(
                    err,
                ) { "Exiting consumer after ${if (!running.get()) "receiving shutdown signal" else "being interrupted by someone"}" }
            } catch (e: Exception) {
                logger.error(
                    e,
                ) {
                    """
                    Unexpected exception while consuming messages. 
                    Stopping consumer, grace period ${graceDuration.seconds / 60} minutes"
                    """.trimIndent()
                }
                startGracePeriod()
                stop()
            } finally {
                if (!running.get()) {
                    logger.warn { "Kafka consumer job is no longer active, consumer has stopped" }
                }
            }
        }
    }

    override fun status(): HealthStatus {
        return if (running.get()) {
            HealthStatus.UP
        } else {
            val currentGrace = grace
            if (currentGrace == null || currentGrace.expired()) {
                HealthStatus.DOWN
            } else {
                HealthStatus.UP
            }
        }
    }

    fun stop() {
        logger.info { "Stopping ${kafkaConsumer.groupMetadata().groupId()} consumer" }
        if (!running.getAndSet(false)) return logger.info { "Already in process of shutting down" }
        logger.info { "Received shutdown signal. Waiting ${grace?.duration?.seconds} seconds for app to shutdown gracefully" }
        kafkaConsumer.wakeup()
    }

    data class Grace(
        val duration: Duration = Duration.ofHours(3),
        val from: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    ) {
        private val expires = from.plus(duration)

        fun expired() = ZonedDateTime.now(ZoneOffset.UTC).isAfter(expires)
    }

    internal companion object {
        fun kafkaConsumer(groupId: String): KafkaConsumer<String, String> {
            val kafkaConfig = AivenConfig.default
            val factory = ConsumerProducerFactory(kafkaConfig)
            val defaultConsumerProperties =
                Properties().apply {
                    this[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
                    this[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false"
                    this[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 10
                }

            return factory.createConsumer(groupId, defaultConsumerProperties)
        }
    }
}
