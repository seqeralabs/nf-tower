FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.1.13-alpine-slim
RUN apk update && apk upgrade && apk add bash
COPY build/libs/*.jar /tower/watchtower-service.jar
COPY wait-for-it.sh /usr/local/bin/wait-for-it.sh
COPY backend-up.sh /usr/local/bin/backend-up.sh
EXPOSE 8080
CMD backend-up.sh