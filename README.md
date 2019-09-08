# nf-tower ![build status](https://codebuild.eu-west-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoid1VqblVBMmVDbE54MUdrTUNra0l5eGl3WWcxR0xCaDU1UVFJV1IzRWdodTJNNmx0d2Q3SS84REdaN1BOTUg4VVd5bS9Xdk8zeW5leFRON1NRZTZSVzhvPSIsIml2UGFyYW1ldGVyU3BlYyI6InVyaGJMWktuOGpDVDQ0WGsiLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=master)

Nextflow Tower system

## Build the environment 

    make build

## Run locally

    make run

See: `docker-compose.yml` file for details


## Backend settings  

Tower backend settings can be provided either:
  - `application.yml` in the backend class-path
  - `tower.yml` in the launching directory

A minimal config requires the settings for the SMTP 
server, using the following variables: 

- TOWER_SMTP_HOST: The SMTP server host name eg. `email-smtp.eu-west-1.amazonaws.com`.
- TOWER_SMTP_PORT: The SMTP server port number eg. `587`.
- TOWER_SMTP_USER: The SMTP user name.  
- TOWER_SMTP_PASSWORD: The SMTP user password.
 

## Basic use case
    
Navigate to GUI in `http://localhost:8000` and follow the instructions.

# Development 

### Backend execution 

Define the following env variables: 

- TOWER_SMTP_USER=<smtp user name>
- TOWER_SMTP_PASSWORD=<smpt password>

(see `tower-backend/src/main/resources/application.yml` for further config details)

Launch the backend with the command: 

```
./gradlew tower-backend:run --continuous
```

### Frontend execution 

```
cd tower-web
npm install
npm run livedev
```

## Database 

Tower is designed to be database agnostic and can use most popular SQL 
database server, such as MySql, Postgres, Oracle and many other. 

By default it uses a [H2](https://www.h2database.com) embedded database meant to be used only for 
evaluation purpose. 


## Environment variables: 

* TOWER_APP_NAME: Application name
* TOWER_SERVER_URL: Server URL eg `https://tower.nf`
* TOWER_CONTACT_EMAIL: Sysadmin email contact eg `hello@tower.nf`
* TOWER_DB_CREATE: DB creation policy eg `none`
- TOWER_DB_URL: Database JDBC connection URL eg. `jdbc:mysql://localhost:3307/tower`. 
- TOWER_DB_DRIVER: Database JDBC driver class name e.g. `com.mysql.cj.jdbc.Driver`.
- TOWER_DB_DIALECT: Database SQL Hibernate dialect `org.hibernate.dialect.MySQL55Dialect`.   
- TOWER_DB_USER: Database user name.
- TOWER_DB_PASSWORD: Database user password.
* TOWER_SMTP_HOST: SMTP server host name
* TOWER_SMTP_PORT: SMTP server port eg 587
* TOWER_SMTP_AUTH: SMTP server authentication eg `true`
* TOWER_SMTP_USER: SMTP server user name 
* TOWER_SMTP_PASSWORD: SMTP server user password 
* TOWER_LOG_LEVEL: Logging level
* TOWER_LOG_MAX_SIZE: Log files max allowed size
* TOWER_LOG_MAX_HISTORY: Max number of rolling log files

