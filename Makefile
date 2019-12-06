.PHONY: run build

install-deps:
	docker pull redis:5.0.7

build:
	./gradlew jibDockerBuild --image=fridaymastermix/st_challange

run: install-deps build
	docker run -d -p 6379:6379 redis
	docker run -d -p 8080:8080 --net=host fridaymastermix/st_challange

