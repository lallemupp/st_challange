.PHONY: run clean

clean:
	./gradlew clean

test:
	./gradlew test

build:
	./gradlew build

install-deps:
	docker pull redis:5.0.7

docker:
	docker build -t fridaymastermix/st_challange .

run: install-deps build docker
	docker run -d -p 6379:6379 redis; exit 0
	docker run -d -p 8080:8080 --net=host fridaymastermix/st_challange

