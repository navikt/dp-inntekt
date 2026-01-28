package no.nav.dagpenger.inntekt.subsumsjonbrukt

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import no.nav.dagpenger.inntekt.Config
import no.nav.dagpenger.inntekt.HealthCheck
import no.nav.dagpenger.inntekt.HealthStatus
import no.nav.dagpenger.inntekt.InntektApiConfig
import no.nav.dagpenger.inntekt.db.InntektId
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.serder.inntektObjectMapper
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.CommitFailedException
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Properties
import kotlin.coroutines.CoroutineContext

internal class KafkaSubsumsjonBruktDataConsumer(
    private val config: InntektApiConfig,
    private val inntektStore: InntektStore,
    private val graceDuration: Duration = Duration.ofHours(3),
) : CoroutineScope,
    HealthCheck {
    private val logger = KotlinLogging.logger { }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private var grace: Grace? = null

    private fun startGracePeriod() {
        if (grace == null) {
            grace = Grace(duration = graceDuration)
            logger.warn { "Grace period started, expires in ${graceDuration.seconds / 60} minutes" }
        }
    }

    private val job: Job by lazy {
        SupervisorJob()
    }

    fun listen() {
        launch {
            logger.info { "Starting ${config.application.id}" }

            KafkaConsumer<String, String>(
                consumerConfig(
                    groupId = config.application.id,
                    bootstrapServerUrl = config.application.brokers,
                    credential = config.application.credential,
                ).also {
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

                        try {
                            ids.forEach { id ->
                                if (inntektStore.markerInntektBrukt(id) == 1) {
                                    logger.info { "Marked inntekt with id $id as used" }
                                }
                            }
                            if (ids.isNotEmpty()) {
                                consumer.commitSync()
                            }
                        } catch (e: CommitFailedException) {
                            logger.warn(e) { "Kafka threw a commit fail exception, looping back" }
                        }
                    }
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
                    if (!job.isActive) {
                        logger.warn { "Kafka consumer job is no longer active, consumer has stopped" }
                    }
                }
            }
        }
    }

    override fun status(): HealthStatus {
        return if (job.isActive) {
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

    companion object {
        private val defaultConsumerConfig =
            Properties().apply {
                put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
                put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            }

        internal fun commonConfig(
            bootstrapServers: String,
            credential: Config.KafkaAivenCredentials? = null,
        ): Properties =
            Properties().apply {
                put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
                credential?.let { creds ->
                    putAll(
                        Properties().apply {
                            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, creds.securityProtocolConfig)
                            put(
                                SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG,
                                creds.sslEndpointIdentificationAlgorithmConfig,
                            )
                            put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, creds.sslTruststoreTypeConfig)
                            put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, creds.sslKeystoreTypeConfig)
                            put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, creds.sslTruststoreLocationConfig)
                            put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, creds.sslTruststorePasswordConfig)
                            put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, creds.sslKeystoreLocationConfig)
                            put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, creds.sslKeystorePasswordConfig)
                        },
                    )
                }
            }
    }

    private fun consumerConfig(
        groupId: String,
        bootstrapServerUrl: String,
        credential: Config.KafkaAivenCredentials? = null,
        properties: Properties = defaultConsumerConfig,
    ): Properties =
        Properties().apply {
            putAll(properties)
            putAll(commonConfig(bootstrapServerUrl, credential))
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        }
}
