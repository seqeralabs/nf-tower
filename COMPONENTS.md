# Tower components

## Angular front-end 

  http://localhost:8000 (dev 4200) 

Config:

  - tower-web/src/environments/environment.prod.ts     [prod]
  - tower-web/src/environments/environment.livedev.ts  [dev]
  - tower-web/angular.json

## Proxy 

  http://localhost:8000:/api/* -> http://backend:8080/*  

Config: 
  - tower-web/nginx.conf       [prod]
  - tower-web/proxy.conf.json  [dev]

## Backend

  http://localhost:8080/*  
  
Config:
  - tower-backend/src/main/resources/application.yml
  - tower-backend/src/main/resources/application-livedev.yml

 

## Email authentication

- User receive an email containing a link to <host>/auth?email=<email>&authToken=<authToken>
  (see UserServiceImpl#buildAccessUrl)

- The link is handled by the front-end which make a post. See `auth.component.ts`

EMAIL -- (HOST/auth?email=XX&authToken=YY) --> FRONTEND -- (POST: HOST/login?username=email&password=authToken) --> BACKEND --> AuthenticationProviderByAuthToken


## Landing page 

It's static html page contained at the path: 
  
  tower-web/src/assets/landing/

The landing page is served via a iframe. The only change in the static page is the need for `target="_parent"` 
in the login links.
