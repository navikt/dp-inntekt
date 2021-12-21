package no.nav.dagpenger.inntekt

import kotlinx.coroutines.delay
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.ArbeidsInntektMaaned
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentRequest
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektkomponentResponse
import no.nav.dagpenger.inntekt.inntektskomponenten.v1.InntektskomponentClient
import no.nav.dagpenger.inntekt.opptjeningsperiode.Opptjeningsperiode
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

internal class Uttrekksjobb(private val dataSource: DataSource, private val client: InntektskomponentClient) {

    private val inntekter = setOf(
        "01DP8G798K6RHW42XCDHQRB706",
        "01DP8DWGXVH2HKFW0BJM4WN9TF",
        "01DPARVSWZPWB1M711VQD07PNQ",
        "01DXJNSNDCV1Y7E273DNKK8NAY",
        "01ERPHTNPSPJAKZ3Y8ZMNN0J6D"
    )

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    internal suspend fun hentInntekterOgSjekk() {
        delay(TimeUnit.SECONDS.toMillis(10))
        val stringBuilder = StringBuilder().append(System.lineSeparator()).append("***********************************************")
        inntekter.forEach { inntektId ->
            val result = using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        "SELECT kontekstid, beregningsdato, aktørId FROM inntekt_v1_person_mapping WHERE inntektid = :id ",
                        mapOf(
                            "id" to inntektId
                        )
                    ).map {
                        Result(
                            inntektsId = inntektId,
                            beregningsdato = it.localDate("beregningsdato"),
                            vedtakId = it.int("kontekstid"),
                            aktørId = it.string("aktørId"),
                        )
                    }.asSingle
                ) ?: throw IllegalStateException("Kunne ikke hendte id")
            }
            val inntekt: InntektkomponentResponse = client.getInntekt(
                InntektkomponentRequest(
                    aktørId = result.aktørId,
                    månedFom = result.opptjeningsperiode.sisteAvsluttendeKalenderMåned,
                    månedTom = result.opptjeningsperiode.førsteMåned
                )
            )
            val sisteMnd: ArbeidsInntektMaaned? =
                inntekt.arbeidsInntektMaaned?.maxByOrNull { it.aarMaaned }

            stringBuilder.append(System.lineSeparator()).append("Inntekt").append("\t")

            if (sisteMnd == null) {

                stringBuilder.append("Tom arbeids inntekt maaned $inntektId")
            } else if (sisteMnd.arbeidsInntektInformasjon == null) {
                stringBuilder.append("Tom arbeidsInntektInformasjon $inntektId")
            } else if (sisteMnd.arbeidsInntektInformasjon.inntektListe?.isEmpty() == true) {
                stringBuilder.append("Tom inntektList $inntektId")
            } else if (sisteMnd.arbeidsInntektInformasjon.inntektListe?.isEmpty() == false) {
                stringBuilder.append("Ikke tom inntektList $inntektId")
            }
        }
        stringBuilder.append(System.lineSeparator()).append("***********************************************")
        logger.info {
            stringBuilder.toString()
        }
    }

    private data class Result(
        val inntektsId: String,
        val beregningsdato: LocalDate,
        val vedtakId: Int,
        val aktørId: String
    ) {
        val opptjeningsperiode = Opptjeningsperiode(beregningsdato)
    }
}
