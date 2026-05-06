# AGENTS.md — dp-inntekt

Dette repoet inneholder **dp-inntekt**, en backend-tjeneste i dagpenger-domenet som henter, lagrer og klassifiserer inntektsopplysninger fra Inntektskomponenten (a-inntekt).

## Arkitektur

| Komponent | Teknologi |
|-----------|-----------|
| Backend API | Kotlin + Ktor (`naisful-app` fra `tbd-libs`) |
| Database | PostgreSQL 15 (Cloud SQL via Nais) + Flyway migrasjoner |
| Kafka | Kafka-consumer for subsumsjonbrukt-hendelser |
| Auth | Azure AD (service-to-service, ingen brukerkontext) |
| Observability | Prometheus (`/metrics`), OpenTelemetry auto-instrumentering, Loki + Elastic logging |

## Modulstruktur

```
dp-inntekt/
├── dp-inntekt-api/          # Hovedapplikasjon (Ktor, Kafka, DB)
│   └── src/main/kotlin/no/nav/dagpenger/inntekt/
│       ├── Application.kt           # Entry point
│       ├── InntektApiConfig.kt      # Konfigurasjon (Konfig)
│       ├── api/v1/                  # Ktor-routes
│       ├── db/                      # PostgreSQL-tilgang (Kotliquery)
│       ├── inntektskomponenten/     # Klient mot a-inntekt
│       ├── klassifiserer/           # Inntektsklassifisering
│       ├── mapping/                 # Domenemapping
│       ├── oppslag/                 # PDL og Enhetsregisteret
│       ├── opptjeningsperiode/      # Opptjeningsperiode-beregning
│       └── subsumsjonbrukt/         # Kafka-consumer
└── dp-inntekt-kontrakter/   # Delte datamodeller (JAR-artefakt)
```

## Viktige konvensjoner

### Personvern og sikkerhet
- **Aldri** logg fødselsnummer (fnr), navn eller andre personidentifiserende opplysninger
- Bruk `aktørId` eller interne ID-er i logger
- Tjenesten behandler **sensitive personopplysninger** (inntektsdata, fnr) — vurder alltid personvernkonsekvenser ved endringer

### Auth
- Tjenesten bruker **Azure AD client_credentials** (ingen brukerkontext)
- Innkommende kall valideres med JWT fra Azure AD
- Kalles av: `dp-oppslag-inntekt`, `dp-arena-sink`, `dp-inntekt-klassifiserer`, `dp-inntekt-frontend`, `dagpenger-regel-ui`

### Database
- Bruk **Kotliquery** for databasetilgang — ikke Exposed eller JPA
- HikariCP pool: `maximumPoolSize = 3` (ikke standardverdien 10)
- Alle skjemaendringer via Flyway-migrasjoner i `src/main/resources/db/migration/`

### Testing
- Testframework: **Kotest** (assertions) + **JUnit 5** (runner)
- Integrasjonstester mot ekte Postgres via **TestContainers**
- HTTP-mocking med **WireMock**
- Auth-mocking med **mock-oauth2-server**
- Kjør tester med: `./gradlew test`

### Bygg og deploy
- Bygg: `./gradlew build`
- Docker: `docker build -t dp-inntekt-api .`
- Nais-konfig: `.nais/nais.yaml` med variabelfiler `vars-dev.yaml`, `vars-prod.yaml`

## Eksterne avhengigheter

| Tjeneste | Formål |
|----------|--------|
| Inntektskomponenten (a-inntekt) | Henter inntektsopplysninger |
| PDL (Persondataløsningen) | Oppslag på person (aktørId ↔ fnr) |
| Enhetsregisteret | Oppslag på arbeidsgiverinformasjon |
| dp-behandling | Intern dagpenger-tjeneste |
| Kafka | Mottar subsumsjonbrukt-hendelser |

## Nais-konfigurasjon

- **Team:** `teamdagpenger`
- **Namespace:** `teamdagpenger`
- **Port:** 8099
- **Helse-endepunkter:** `/isalive`, `/isready`, `/metrics`
- **SQL:** PostgreSQL 15 Cloud SQL med pgaudit aktivert
