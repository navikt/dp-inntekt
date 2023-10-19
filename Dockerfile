FROM ghcr.io/navikt/baseimages/temurin:21

EXPOSE 50051

COPY dp-inntekt-api/build/libs/dp-inntekt-api-all.jar /app/app.jar