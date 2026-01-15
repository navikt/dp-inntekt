package no.nav.dagpenger.inntekt.serder

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.math.BigDecimal

fun ObjectMapper.configure() =
    this.apply {
        registerModules(
            KotlinModule
                .Builder()
                .withReflectionCacheSize(512)
                .build(),
        )
        registerModules(
            SimpleModule().also { module ->
                module.addSerializer(BigDecimal::class.java, ToStringSerializer())
            },
        )

        registerModules(JavaTimeModule())
        configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

internal val jacksonObjectMapper =
    jacksonObjectMapper().configure()
