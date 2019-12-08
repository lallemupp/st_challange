FROM gradle:6.0.1-jdk13
VOLUME /tmp
ADD . /project
WORKDIR /project
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} st_challange.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","st_challange.jar"]
