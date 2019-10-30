<img src='./tower-web/src/assets/landing/assets/img/nf-tower-purple.svg' width='500' alt='Nextflow Tower logo'/>

[![Chat on Gitter](https://img.shields.io/gitter/room/nf-tower/community.svg?colorB=26af64&style=popout)](https://gitter.im/nf-tower/community)

# Intro

Nextflow Tower is an open source monitoring and management platform for [Nextflow](https://www.nextflow.io/) workflows. Tower can be used to track your workflows wherever they run either locally, on your cluster, in the cloud, or any combination of these. 

Learn more at [tower.nf](https://tower.nf/).

# Running locally

Nextflow Tower can be run locally using Docker, and requires only configuration of an SMTP for login emails.

## Backend settings

The backend can be configured in one of two places:

* `application.yml` in the backend class-path (`tower-backend/src/main/resources/application.yml`)
* `tower.yml` in the launching directory

An example minimal `tower.yml` is provided below.

``` yaml
tower:
    contactEmail: nftower@example.com
---
mail:
  from: nftower@example.com
  smtp:
    host: smtp.example.com
    port: 587
    user: username
    password: password
```

## Build the environment

``` shellsession
make build
```

## Run locally

``` shellsession
make run
```

See: `docker-compose.yml` file for details

## Basic use case

Navigate to the GUI at `http://localhost:8000` and follow the instructions to run your workflow with Tower.

# Development

## Backend execution

Define the following env variables:

* TOWER_SMTP_USER=`<smtp user name>`
* TOWER_SMTP_PASSWORD=`<smtp password>`

(see `tower-backend/src/main/resources/application.yml` for further config details)

Launch the backend with the command:

``` shellsession
./gradlew tower-backend:run --continuous
```

## Frontend execution

``` shellsession
cd tower-web
npm install
npm run livedev
```

## Database

Tower is designed to be database agnostic and can use most popular SQL database servers, such as MySQL, Postgres, Oracle and many others.

By default it uses [H2](https://www.h2database.com), an embedded database meant to be used for evaluation purpose only.

## Environment variables

* TOWER_APP_NAME: Application name.
* TOWER_SERVER_URL: Server URL e.g. `https://tower.nf`.
* TOWER_CONTACT_EMAIL: Sysadmin email contact e.g. `hello@tower.nf`.
* TOWER_DB_CREATE: DB creation policy e.g. `none`.
* TOWER_DB_URL: Database JDBC connection URL e.g. `jdbc:mysql://localhost:3307/tower`.
* TOWER_DB_DRIVER: Database JDBC driver class name e.g. `com.mysql.cj.jdbc.Driver`.
* TOWER_DB_DIALECT: Database SQL Hibernate dialect `org.hibernate.dialect.MySQL55Dialect`.
* TOWER_DB_USER: Database user name.
* TOWER_DB_PASSWORD: Database user password.
* TOWER_SMTP_HOST: SMTP server host name.
* TOWER_SMTP_PORT: SMTP server port e.g. `587`.
* TOWER_SMTP_AUTH: SMTP server authentication eg `true`
* TOWER_SMTP_USER: SMTP server user name.
* TOWER_SMTP_PASSWORD: SMTP server user password.

## Support

* For common problems, doubts, and feedback please use the [Gitter community channel](https://gitter.im/nf-tower/community) or the [GitHub issues page](https://github.com/seqeralabs/nf-tower/issues).
* Commercial support is provided by [Seqera Labs](https://www.seqera.io/).

## License

[Mozilla Public License v2.0](LICENSE.txt)
