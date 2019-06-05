# Tower components

## Angular front-end 

  http://localhost:8000 (dev 4200) 

Config:

  - watchtower-gui/src/environments/environment.prod.ts     [prod]
  - watchtower-gui/src/environments/environment.livedev.ts  [dev]
  - watchtower-gui/angular.json

## Proxy 

  http://localhost:8000:/api/* -> http://backend:8080/*  

Config: 
  - watchtower-gui/nginx.conf       [prod]
  - watchtower-gui/proxy.conf.json  [dev]

## Backend

  http://localhost:8080/*  
  
Config:
  - watchtower-service/src/main/resources/application.yml
  - watchtower-service/src/main/resources/application-livedev.yml

## MongoDB

environment:

  - MONGO_HOST: mongo
  - MONGO_PORT: 27017
 
