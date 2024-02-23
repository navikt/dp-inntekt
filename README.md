# dagpenger-inntekt-api
Holder kopi av inntekter fra inntektskomponenten for regelkjøring av dagpenger-behov

Er API for inntektsredigeringsverkøyet i https://github.com/navikt/dagpenger-regel-ui


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

### Personlig tilgang til Postgres databasen

Se [Personlig tilgang](https://docs.nais.io/how-to-guides/persistence/postgres/#personal-database-access)



