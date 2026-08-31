package no.nav.dagpenger.inntekt.serder

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.introspect.DefaultAccessorNamingStrategy
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder

/*
registerModules(
    SimpleModule().also { module ->
        module.addSerializer(BigDecimal::class.java, ToStringSerializer())
    },
)*/

val inntektObjectMapper: JsonMapper =
    jacksonMapperBuilder()
        .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
        // Bugfix: Dropper felter som begynner på Æ/Ø/Å
        .accessorNaming(DefaultAccessorNamingStrategy.Provider().withFirstCharAcceptance(true, true))
        .build()
