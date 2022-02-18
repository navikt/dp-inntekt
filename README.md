# dagpenger-inntekt-api
Holder kopi av inntekter fra inntektskomponenten for regelkjøring av dagpenger-behov

Er API for inntektsredigeringsverkøyet i https://github.com/navikt/dagpenger-regel-ui

## Tilgangskontroll

Endepunktet `/inntekt` er sikret med api nøkler. 

Klienter må ha registrert en api nøkkel og ha kjennskap til felles hemmelighet. Hemmelighet og api nøkkel må ligge et sikkert sted (vault)

Lag en ny api nøkkel ved å kjøre: 

```bash
cat /dev/urandom | env LC_CTYPE=C tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1
```


Eksempel på å generere hemmelighet: 

```bash
openssl rand -base64 8 |md5 |head -c15;echo
```


## Utvikling av applikasjonen

For å kjøre enkelte av testene kreves det at Docker kjører.

[Docker Desktop](https://www.docker.com/products/docker-desktop)


### Starte applikasjonen lokalt

Applikasjonen har avhengigheter til Postgres som kan kjøres
opp lokalt vha Docker Compose(som følger med Docker Desktop) 


Starte Postgres: 
```

docker-compose -f docker-compose.yml up

```
Etter at containerene er startet kan man starte applikasjonen ved å kjøre main metoden.


Stoppe Postgres:

```
ctrl-c og docker-compose -f docker-compose.yml down 

```

### Tilgang til Postgres databasen

For utfyllende dokumentasjon se [Postgres i NAV](https://github.com/navikt/utvikling/blob/master/PostgreSQL.md)

#### Tldr

Applikasjonen benytter seg av dynamisk genererte bruker/passord til database.
For å koble seg til databasen må man genere bruker/passord(som varer i en time)
på følgende måte:

Installere [Vault](https://www.vaultproject.io/downloads.html)

Generere bruker/passord: 

```

export VAULT_ADDR=https://vault.adeo.no USER=NAV_IDENT
vault login -method=oidc


```

Hvis du får noe à la `connection refused`, må du før `vault login -method=oidc` legge til:
```
export HTTPS_PROXY="socks5://localhost:14122" 
export NO_PROXY=".microsoftonline.com,.terraform.io,.hashicorp.com"
```

Preprod credentials:

```
vault read postgresql/preprod-fss/creds/dp-inntekt-db-preprod-admin

```

Prod credentials:

```
vault read postgresql/prod-fss/creds/dp-inntekt-db-admin

```

Bruker/passord kombinasjonen kan brukes til å koble seg til de aktuelle databasene
F.eks

```
-- dev
export DATABASE_NAME=dev-pg.intern.nav.no/dp-inntekt-db-preprod
-- prod
export DATABASE_NAME=prod-pg.intern.nav.no/dp-inntekt-db

psql -d $DATABASE_NAME -h $DATABASE_HOST -U $GENERERT_BRUKER_NAVN

```


