package no.nav.dagpenger.inntekt.rpc

import com.squareup.moshi.JsonAdapter
import io.grpc.Metadata
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.SpesifisertInntekt
import no.nav.dagpenger.inntekt.AuthApiKeyVerifier
import no.nav.dagpenger.inntekt.db.IllegalInntektIdException
import no.nav.dagpenger.inntekt.db.InntektNotFoundException
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.klassifiserer.klassifiserOgMapInntekt
import no.nav.dagpenger.inntekt.moshiInstance

private val logger = KotlinLogging.logger {}

internal class InntektGrpcServer(
    private val port: Int,
    apiKeyVerifier: AuthApiKeyVerifier,
    inntektStore: InntektStore
) {
    private val inntektGrpcApi = InntektGrpcApi(inntektStore).bindService()

    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(inntektGrpcApi)
        .intercept(ApiKeyServerInterceptor(apiKeyVerifier))
        .build()

    fun start() {
        server.start()
        logger.info { "inntekt gRPC server started, listening on $port" }
        Runtime.getRuntime().addShutdownHook(
            Thread {
                logger.info { "*** shutting down gRPC server since JVM is shutting down" }
                stop()
                logger.info { "*** inntekt gRPC server  shut down" }
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}

internal class ApiKeyServerInterceptor(private val apiKeyVerifier: AuthApiKeyVerifier) : ServerInterceptor {
    companion object {
        private val API_KEY_HEADER: Metadata.Key<String> =
            Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER)
    }

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        withLoggingContext("request_type" to "gRPC") {
            val authenticated = headers.get(API_KEY_HEADER)?.let { apiKeyVerifier.verify(it) } ?: false
            if (!authenticated) {
                logger.warn { "gRpc call not authenticated" }
                throw StatusRuntimeException(Status.UNAUTHENTICATED)
            }
        }
        return next.startCall(call, headers)
    }
}

internal class InntektGrpcApi(private val inntektStore: InntektStore) :
    InntektHenterGrpcKt.InntektHenterCoroutineImplBase() {
    companion object {
        val spesifisertInntektAdapter: JsonAdapter<SpesifisertInntekt> =
            moshiInstance.adapter(SpesifisertInntekt::class.java)
        val klassifsertInntektAdapter: JsonAdapter<Inntekt> = moshiInstance.adapter(Inntekt::class.java)
    }

    override suspend fun hentSpesifisertInntektAsJson(request: InntektId): SpesifisertInntektAsJson {
        val inntekt = getSpesifisertInntekt(request)
        return SpesifisertInntektAsJson.newBuilder()
            .setInntektId(inntekt.inntektId.let { InntektId.newBuilder().setId(it.id).build() })
            .setJson(spesifisertInntektAdapter.toJson(inntekt)).build()
    }

    override suspend fun hentKlassifisertInntektAsJson(request: InntektId): KlassifisertInntektAsJson {
        val inntekt = getSpesifisertInntekt(request)
        val klassifisert = klassifiserOgMapInntekt(inntekt)
        return KlassifisertInntektAsJson.newBuilder()
            .setInntektId(inntekt.inntektId.let { InntektId.newBuilder().setId(it.id).build() })
            .setJson(klassifsertInntektAdapter.toJson(klassifisert)).build()
    }

    private fun getSpesifisertInntekt(request: InntektId): SpesifisertInntekt {
        try {
            return inntektStore.getSpesifisertInntekt(request.id.let { no.nav.dagpenger.inntekt.db.InntektId(it) })
        } catch (e: InntektNotFoundException) {
            throw StatusException(Status.NOT_FOUND.withDescription("Inntekt with id ${request.id} not found"))
        } catch (e: IllegalInntektIdException) {
            throw StatusException(Status.INVALID_ARGUMENT.withDescription("Id  ${request.id} not a legal inntekt id"))
        }
    }
}
