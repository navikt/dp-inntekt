package no.nav.dagpenger.inntekt.serder

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.introspect.DefaultAccessorNamingStrategy
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.ToStringSerializer
import tools.jackson.module.kotlin.jacksonMapperBuilder
import java.math.BigDecimal

val inntektObjectMapper: JsonMapper =
    jacksonMapperBuilder()
        .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
        // Bugfix: Dropper felter som begynner på Æ/Ø/Å
        .accessorNaming(DefaultAccessorNamingStrategy.Provider().withFirstCharAcceptance(true, true))
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .addModule(SimpleModule().addSerializer(BigDecimal::class.java, ToStringSerializer(BigDecimal::class.java)))
        .build()
