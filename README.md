# dp-inntekt

Cache-tjeneste for inntektsopplysninger i dagpenger-domenet. Henter inntektsdata fra Inntektskomponenten (a-inntekt), lagrer dem i PostgreSQL og eksponerer dem til dagpenger-regelmotor og saksbehandlingsverktøy.

- **API for inntektsredigeringsverktøyet:** [dagpenger-regel-ui](https://github.com/navikt/dagpenger-regel-ui)
- **Team:** teamdagpenger
- **Plattform:** Nais / GCP

📖 [Arkitektur og integrasjonsdokumentasjon](docs/arkitektur.md)

---

## Utvikling

Docker må kjøre for å kjøre integrasjonstestene.

### Starte applikasjonen lokalt

```bash
# Start PostgreSQL
docker-compose -f docker-compose.yml up

# Kjør applikasjonen (main i Application.kt)
./gradlew :dp-inntekt-api:run

# Stopp PostgreSQL
docker-compose -f docker-compose.yml down
```

### Kjøre tester

```bash
./gradlew test
```

### Personlig tilgang til PostgreSQL i dev/prod

Se [Nais-dokumentasjon: Personlig tilgang](https://docs.nais.io/how-to-guides/persistence/postgres/#personal-database-access)

