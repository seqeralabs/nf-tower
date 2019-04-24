#!/usr/bin/env bash

./gradlew assemble
docker build -t watchtower-service:latest watchtower-service/
docker build -t watchtower-gui:latest watchtower-gui/