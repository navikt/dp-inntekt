FROM gcr.io/distroless/java21

ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"

COPY dp-inntekt-api/build/install/dp-inntekt-api/lib /app/lib

ENTRYPOINT ["java", "-cp", "/app/lib/*", "no.nav.dagpenger.inntekt.ApplicationKt"]