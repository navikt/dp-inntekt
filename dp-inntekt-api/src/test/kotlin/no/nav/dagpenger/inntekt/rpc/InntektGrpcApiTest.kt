package no.nav.dagpenger.inntekt.rpc

import de.huxhorn.sulky.ulid.ULID
import io.grpc.Metadata
import io.grpc.Metadata.ASCII_STRING_MARSHALLER
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.Status
import io.grpc.Status.UNAUTHENTICATED
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.events.inntekt.v1.Aktør
import no.nav.dagpenger.events.inntekt.v1.AktørType
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.SpesifisertInntekt
import no.nav.dagpenger.inntekt.ApiKeyVerifier
import no.nav.dagpenger.inntekt.AuthApiKeyVerifier
import no.nav.dagpenger.inntekt.db.InntektNotFoundException
import no.nav.dagpenger.inntekt.db.InntektStore
import no.nav.dagpenger.inntekt.klassifiserer.klassifiserOgMapInntekt
import no.nav.dagpenger.inntekt.moshiInstance
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.time.YearMonth

internal class InntektGrpcApiTest : GrpcTest() {

    @Test
    fun `Should throw error on illegal inntekt id`() {
        val inntektStore = mockk<InntektStore>(relaxed = true).also {
            every { it.getSpesifisertInntekt(any()) } throws InntektNotFoundException("Not found")
        }
        val client =
            InntektHenterGrpcKt.InntektHenterCoroutineStub(makeChannel(InntektGrpcApi(inntektStore)))
        val request = InntektId.newBuilder().setId("1233").build()

        val exception = assertThrows<StatusException> { runBlocking { client.hentSpesifisertInntektAsJson(request) } }
        exception.status.code shouldBe Status.INVALID_ARGUMENT.code
    }

    @Test
    fun `Should throw error when inntekt do not exist`() {
        val inntektStore = mockk<InntektStore>(relaxed = true).also {
            every { it.getSpesifisertInntekt(any()) } throws InntektNotFoundException("Not found")
        }
        val client =
            InntektHenterGrpcKt.InntektHenterCoroutineStub(makeChannel(InntektGrpcApi(inntektStore)))
        val request = InntektId.newBuilder().setId(ULID().nextULID()).build()

        val exception = assertThrows<StatusException> { runBlocking { client.hentSpesifisertInntektAsJson(request) } }
        exception.status.code shouldBe Status.NOT_FOUND.code
    }

    @Test
    fun `Should fetch spesifisert inntekt `() {
        val inntektId = ULID().nextULID()
        val spesifisertInntekt = SpesifisertInntekt(
            inntektId = no.nav.dagpenger.events.inntekt.v1.InntektId(inntektId),
            avvik = emptyList(),
            posteringer = emptyList(),
            sisteAvsluttendeKalenderMåned = YearMonth.now(),
            ident = Aktør(AktørType.AKTOER_ID, "1234"),
            manueltRedigert = false,
            timestamp = LocalDateTime.now()
        )

        val inntektStore = mockk<InntektStore>(relaxed = true).also {
            every { it.getSpesifisertInntekt(any()) } returns spesifisertInntekt
        }
        val client =
            InntektHenterGrpcKt.InntektHenterCoroutineStub(makeChannel(InntektGrpcApi(inntektStore)))
        val request = InntektId.newBuilder().setId(inntektId).build()

        val inntekt = runBlocking { client.hentSpesifisertInntektAsJson(request) }
        inntekt.inntektId.id shouldBe inntektId
        inntekt.json shouldNotBe null
        val spesifisertInntektResponse = moshiInstance.adapter(SpesifisertInntekt::class.java).fromJson(inntekt.json)!!
        spesifisertInntekt shouldBe spesifisertInntektResponse
    }

    @Test
    fun `Should fetch klassifisert inntekt `() {
        val inntektId = ULID().nextULID()
        val spesifisert = SpesifisertInntekt(
            inntektId = no.nav.dagpenger.events.inntekt.v1.InntektId(inntektId),
            avvik = emptyList(),
            posteringer = emptyList(),
            sisteAvsluttendeKalenderMåned = YearMonth.now(),
            ident = Aktør(AktørType.AKTOER_ID, "1234"),
            manueltRedigert = false,
            timestamp = LocalDateTime.now()
        )
        val klassifisert = klassifiserOgMapInntekt(spesifisert)

        val inntektStore = mockk<InntektStore>(relaxed = true).also {
            every { it.getSpesifisertInntekt(any()) } returns spesifisert
        }
        val client =
            InntektHenterGrpcKt.InntektHenterCoroutineStub(makeChannel(InntektGrpcApi(inntektStore)))
        val request = InntektId.newBuilder().setId(inntektId).build()

        val inntekt = runBlocking { client.hentKlassifisertInntektAsJson(request) }
        inntekt.inntektId.id shouldBe inntektId
        inntekt.json shouldNotBe null
        val klassifisertInntektResponse = moshiInstance.adapter(Inntekt::class.java).fromJson(inntekt.json)!!
        klassifisert.sisteAvsluttendeKalenderMåned shouldBe klassifisertInntektResponse.sisteAvsluttendeKalenderMåned
        klassifisert.inntektsListe shouldBe klassifisertInntektResponse.inntektsListe
        klassifisert.inntektsId shouldBe klassifisertInntektResponse.inntektsId
        klassifisert.manueltRedigert shouldBe klassifisertInntektResponse.manueltRedigert
    }
}

internal class ApiKeyServerInterceptorTest {

    @Test
    fun ` Should throw authenticated exception if no api key present `() {
        val verifier = AuthApiKeyVerifier(ApiKeyVerifier("secret"), listOf("client"))
        val apiKeyServerInterceptor = ApiKeyServerInterceptor(verifier)

        val mockedServerCall = mockk<ServerCall<Any, Any>>(relaxed = true)
        val headers = Metadata()
        val mockedServerCallHandler = mockk<ServerCallHandler<Any, Any>>(relaxed = true)

        val exc = assertThrows<StatusRuntimeException> { apiKeyServerInterceptor.interceptCall(mockedServerCall, headers, mockedServerCallHandler) }
        exc.status.code shouldBe UNAUTHENTICATED.code

        verify(exactly = 0) { mockedServerCallHandler.startCall(mockedServerCall, headers) }
    }

    @Test
    fun ` Should forward authenticated calls `() {
        val keyVerifier = ApiKeyVerifier("secret")
        val apiVerifier = AuthApiKeyVerifier(keyVerifier, listOf("client"))
        val apiKeyServerInterceptor = ApiKeyServerInterceptor(apiVerifier)

        val mockedServerCall = mockk<ServerCall<Any, Any>>(relaxed = true)
        val headers = Metadata()
        headers.put(Metadata.Key.of("x-api-key", ASCII_STRING_MARSHALLER), keyVerifier.generate("client"))
        val mockedServerCallHandler = mockk<ServerCallHandler<Any, Any>>(relaxed = true)

        apiKeyServerInterceptor.interceptCall(mockedServerCall, headers, mockedServerCallHandler)

        verify(exactly = 1) { mockedServerCallHandler.startCall(mockedServerCall, headers) }
    }
}
