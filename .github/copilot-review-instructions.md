# GitHub Copilot — Review-instruksjoner for dp-inntekt

Disse instruksjonene gjelder for automatiske kode-reviews i `dp-inntekt`. Tjenesten behandler **sensitive personopplysninger** og kjører i produksjon på Nais (GCP).

## Alltid sjekk

### 🔴 Personvern og sikkerhet (blokkerende)

- [ ] Logges det fødselsnummer, navn, adresse eller inntektsbeløp koblet til person? → **Blokkerende funn**
- [ ] Er JWT-validering på plass for nye endepunkter?
- [ ] Håndteres feil uten å eksponere intern systeminfo i HTTP-responser?
- [ ] Er nye Flyway-migrasjoner bakoverkompatible (ingen DROP/RENAME uten koordinering)?
- [ ] Er hardkodede secrets, tokens eller passord fraværende?

### 🟡 Databasetilgang

- [ ] Brukes Kotliquery (ikke Exposed, JPA eller raw JDBC string-bygging)?
- [ ] Er HikariCP `maximumPoolSize` satt til 3–5 (ikke standardverdien 10)?
- [ ] Er `idleTimeout` og `maxLifetime` konfigurert eksplisitt?
- [ ] Er Flyway-migrasjoner navngitt korrekt (`V{nr}__{beskrivelse}.sql`)?

### 🟡 Ktor og API

- [ ] Returnerer feilhåndtering `ProblemDetails` (RFC 7807) — se `Problem.kt`?
- [ ] Er nye routes registrert i Application.kt / routing-oppsett?
- [ ] Er Content-Type satt korrekt (JSON)?

### 🟡 Testing

- [ ] Er det tester for ny forretningslogikk?
- [ ] Brukes Kotest-assertions (`shouldBe`, `shouldNotBeNull`) konsistent?
- [ ] Brukes `mock-oauth2-server` eller `JwtStub` for auth i tester (ikke mock-fri)?
- [ ] Er TestContainers brukt for Postgres-integrasjonstester?

### 🟢 Kode-kvalitet

- [ ] Er Kotlin-idiomet fulgt (data class, sealed class, extension functions)?
- [ ] Er det unødvendige `!!`-operatorer (null-unsafe)?
- [ ] Er logging gjort med `KotlinLogging.logger {}`?
- [ ] Er konfigurasjon lest via `Konfig` (ikke `System.getenv()` direkte)?

## Nais-manifest

Hvis `.nais/nais.yaml` eller variabelfiler endres:

- [ ] Ingen `cpu: limits` (kun `requests`)
- [ ] `accessPolicy.inbound` er eksplisitt og minimal
- [ ] `accessPolicy.outbound` inkluderer kun nødvendige tjenester
- [ ] Secrets refereres via Nais secret-mekanisme, ikke env-variabler med hardkodede verdier

## Kafka

Hvis Kafka-kode endres:

- [ ] Er consumer-group konfigurert (ikke tilfeldig generert)?
- [ ] Håndteres deserialiseringsfeil (dead-letter / logging uten PII)?
- [ ] Er `poll`-timeout og `max.poll.records` vurdert?

## Når du er usikker

Kommenter med forslag og spørsmål fremfor å blokkere. Bruk prefiks:
- `[BLOKKERENDE]` — må fikses før merge (sikkerhet, personvern, dataintegritet)
- `[ANBEFALING]` — bør fikses, men ikke blokkerende
- `[SPØRSMÅL]` — be om avklaring
