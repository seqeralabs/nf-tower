# nf-tower ![build status](https://codebuild.eu-west-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiK1E3NmtuOUVrbElTUlBxbHhRY0MvUzNjUVI2bFFMd25HR0xYb2k0T0JkdzJaMU9Ycmw0dHhNK0hzNHVybUhaaFVtQVY0WUdKM0wyY2ZJcHRyTzE0UmE4PSIsIml2UGFyYW1ldGVyU3BlYyI6IkNWdkFVdVUweEFvZWo4QmwiLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=master)

Nextflow Tower system

## Build the environment 

    make build

## Running locally

    make run

See: `docker-compose.yml` file

## Launch stack in dev mode

    make dev-up 

See: `docker-livedev.yml` file for details. 


## Backend settings  

Tower backend settings can be provided either:
  - `application.yml` in the backend class-path
  - `tower.yml` in the launching directory

A minimal config requires the settings for the SMTP 
server, for example: 

```yml
mail:
  smtp:
    host: email-smtp.eu-west-1.amazonaws.com
    port: 587
    auth: true
    user: <replace with your user id>
    password: <replace with your password>
    starttls:
      enable: true
      required: true
```

## Basic use case
    
Navigate to GUI in `http://localhost:8000` and follow the instructions.


## Tower Mock Server 

https://gist.github.com/pditommaso/847cac01446dc3bc57d7fe5d7a0227d1

