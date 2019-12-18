FROM openjdk:8-alpine
RUN apk update && apk upgrade && apk add bash
COPY build/libs/tower-*-all.jar /tower/tower-backend-all.jar
COPY tower.sh /usr/local/bin/tower.sh
RUN wget https://github.com/patric-r/jvmtop/releases/download/0.8.0/jvmtop-0.8.0.tar.gz -O- | tar xz \
 && chmod +x jvmtop.sh \
 && mv jvmtop.* /usr/local/bin/
EXPOSE 8080
CMD tower.sh
