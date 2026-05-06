# GitHub Copilot — instruksjoner for dp-inntekt

## Prosjektoversikt

`dp-inntekt` er en Kotlin/Ktor-backend i dagpenger-domenet som henter, lagrer og klassifiserer inntektsopplysninger fra Inntektskomponenten (a-inntekt). Tjenesten kjører på Nais (GCP) og behandler sensitive personopplysninger.

## Teknisk stack

- **Språk:** Kotlin
- **Rammeverk:** Ktor via `naisful-app` (fra `com.github.navikt.tbd-libs`)
- **Database:** PostgreSQL 15 + Flyway + Kotliquery + HikariCP
- **Kafka:** Kafka-consumer (subsumsjonbrukt)
- **Auth:** Azure AD (JWT-validering, service-to-service)
- **Testing:** Kotest + JUnit 5 + TestContainers + WireMock + mock-oauth2-server
- **Bygg:** Gradle (Kotlin DSL)

## Kodestil og konvensjoner

### Generelt
- Skriv idiomatisk Kotlin — foretrekk `data class`, `sealed class`, extension functions og `when` fremfor Java-stil
- Bruk `KotlinLogging.logger {}` (fra `io.github.oshai.kotlinlogging`) for logging
- Konfigurasjon via `Konfig`-biblioteket, ikke miljøvariabler direkte
- Bruk `Result`-typer eller sealed classes for feilhåndtering fremfor unchecked exceptions der det er naturlig

### Database
- Bruk **Kotliquery** for alle databaseoperasjoner — aldri Exposed eller JPA/Hibernate
- HikariCP `maximumPoolSize` skal være **3** (ikke standardverdien 10) i containermiljø
- Sett alltid `idleTimeout`, `maxLifetime` og `connectionTimeout` eksplisitt
- Skjemaendringer kun via Flyway-migrasjoner i `src/main/resources/db/migration/`
- Migrasjonsfiler navngis: `V{versjon}__{beskrivelse}.sql`

### Personvern — KRITISK
- **Aldri** logg fødselsnummer (fnr), navn, adresse eller andre personidentifiserende opplysninger
- Logg heller interne ID-er (f.eks. `inntektId`, `aktørId` fra kontekst)
- Vurder alltid GDPR-implikasjoner ved endringer som berører persondata

### Auth
- Tjenesten bruker Azure AD client_credentials (M2M)
- Ikke introduser brukerkontext (ID-porten/TokenX) uten eksplisitt avklaring
- Valider alltid JWT-krav (`aud`, `iss`) i nye endepunkter

### API-design
- Følg eksisterende mønster i `api/v1/` for nye routes
- Bruk `ProblemDetails` (RFC 7807) for feilresponser — se `Problem.kt`
- Returner HTTP 400 for ugyldige forespørsler, 404 for ikke-funnet, 502 for upstream-feil

### Testing
- Bruk **Kotest** for assertions: `shouldBe`, `shouldNotBeNull`, osv.
- Bruk **JUnit 5** som test-runner (`@Test`-annotasjon)
- Integrasjonstester mot Postgres via `@Testcontainers` — se `Postgres.kt` i test-mappen
- Mock HTTP-kall med WireMock, ikke hardkodede responser
- Auth i tester: bruk `JwtStub` eller `mock-oauth2-server`
- Ikke skriv tester som er avhengige av ekstern tilstand

## Hva Copilot bør unngå

- ❌ Ikke sett `cpu: limits` i Nais-manifest (bruk kun `requests`)
- ❌ Ikke logg PII (fnr, navn, adresse, inntektsbeløp koblet til person)
- ❌ Ikke bruk HikariCP `maximumPoolSize = 10` (standard) — bruk 3 i containere
- ❌ Ikke introduser nye avhengigheter uten å bruke `dp-version-catalog`
- ❌ Ikke hardkod URL-er, credentials eller miljøspesifikk konfig i kode

## Nyttige referanser i kodebasen

| Fil | Innhold |
|-----|---------|
| `Application.kt` | Entry point, app-oppsett med `naisApp` |
| `InntektApiConfig.kt` | All konfigurasjon samlet |
| `db/PostgresDataSourceBuilder.kt` | HikariCP + Flyway oppsett |
| `api/v1/UklassifisertInntektRoute.kt` | Eksempel på Ktor-route med auth |
| `JwtStub.kt` (test) | Auth-stub for tester |
| `Postgres.kt` (test) | TestContainers Postgres-oppsett |
