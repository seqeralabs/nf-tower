clean:
	./gradlew clean
	docker rm nf-tower_mongo_1 || true

test:
	./gradlew test

build:
	./gradlew assemble
	docker build -t watchtower-service:latest watchtower-service/
	docker build -t watchtower-gui:latest watchtower-gui/

run:
	docker-compose up --build
