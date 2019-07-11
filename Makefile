config ?= compile

clean:
	./gradlew clean
	docker rm nf-tower_db_1 || true

test:
ifndef class
	MICRONAUT_ENVIRONMENTS=mysql ./gradlew test
else
	MICRONAUT_ENVIRONMENTS=mysql ./gradlew test --tests ${class}
endif

build:
	./gradlew assemble
	docker build -t tower-backend:latest tower-backend/
	docker build -t watchtower-gui:latest watchtower-gui/

run:
	docker-compose up --build

deps:
	./gradlew -q tower-backend:dependencies --configuration ${config}

dev-up:
	docker-compose -f docker-livedev.yml up --build
	echo Open your browser --> http://localhost:4200/

dev-down:
	docker-compose -f docker-livedev.yml down