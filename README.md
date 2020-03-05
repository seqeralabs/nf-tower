<img src='./tower-web/src/assets/landing/assets/img/nf-tower-purple.svg' width='500' alt='Nextflow Tower logo'/>

[![Chat on Gitter](https://img.shields.io/gitter/room/nf-tower/community.svg?colorB=26af64&style=popout)](https://gitter.im/nf-tower/community)

Nextflow Tower is an open source monitoring and managing platform
for [Nextflow](https://www.nextflow.io/) workflows. Learn more at [tower.nf](https://tower.nf/).

## Requirements 

* Java 8
* Docker engine

## Build the environment

```bash
make build
```

## Run locally

```bash
make run
```

See `docker-compose.yml` file for details.


## Backend settings

Tower backend settings can be provided in either:

- `application.yml` in the backend class-path
- `tower.yml` in the launching directory

A minimal config requires the settings for the SMTP
server, using the following variables:

- `TOWER_SMTP_HOST`: The SMTP server host name e.g. `email-smtp.eu-west-1.amazonaws.com`.
- `TOWER_SMTP_PORT`: The SMTP server port number e.g. `587`.
- `TOWER_SMTP_USER`: The SMTP user name.
- `TOWER_SMTP_PASSWORD`: The SMTP user password.

## Basic use case

Navigate to GUI in `http://localhost:8000` and follow the instructions.

# Development

### Backend execution

Define the following env variables:

- `TOWER_SMTP_USER=<smtp user name>`
- `TOWER_SMTP_PASSWORD=<smpt password>`

See `tower-backend/src/main/resources/application.yml` for further config details.

Launch the backend with the command:

```bash
./gradlew tower-backend:run --continuous
```

### Frontend execution

```bash
cd tower-web
npm install
npm run livedev
```

## Database

Tower is designed to be database agnostic and can use most popular SQL
database servers, such as MySql, Postgres, Oracle and many other.

By default it uses [H2](https://www.h2database.com), an embedded database meant to be used for evaluation purpose only.


## Environment variables:

* `TOWER_APP_NAME`: Application name.
* `TOWER_SERVER_URL`: Server URL e.g. `https://tower.nf`.
* `TOWER_CONTACT_EMAIL`: Sysadmin email contact e.g. `hello@tower.nf`.
* `TOWER_DB_CREATE`: DB creation policy e.g. `none`.
* `TOWER_DB_URL`: Database JDBC connection URL e.g. `jdbc:mysql://localhost:3307/tower`.
* `TOWER_DB_DRIVER`: Database JDBC driver class name e.g. `com.mysql.cj.jdbc.Driver`.
* `TOWER_DB_DIALECT`: Database SQL Hibernate dialect `org.hibernate.dialect.MySQL55Dialect`.
* `TOWER_DB_USER`: Database user name.
* `TOWER_DB_PASSWORD`: Database user password.
* `TOWER_SMTP_HOST`: SMTP server host name.
* `TOWER_SMTP_PORT`: SMTP server port e.g. `587`.
* `TOWER_SMTP_AUTH`: SMTP server authentication eg `true`
* `TOWER_SMTP_USER`: SMTP server user name.
* `TOWER_SMTP_PASSWORD`: SMTP server user password.

## Support 

* For common problems, doubts and feedback please use the [Gitter community channel](https://gitter.im/nf-tower/community) 
  or the [GitHub issues page](https://github.com/seqeralabs/nf-tower/issues). 
* This source code is distributed as it is for community adoption. 
* Distribution packages, deployment scripts, maintenance updates, migration scripts and custom integrations are available to customers of [Seqera Labs](https://seqera.io/).
 

## License

[Mozilla Public License v2.0](LICENSE.txt)
