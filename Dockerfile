FROM openjdk:17

WORKDIR /app

ADD target/guides-0.0.1-SNAPSHOT.jar backend.jar
ADD .env .

RUN ["mkdir", "static"]
RUN ["mkdir", "static/profiles"]

ENTRYPOINT ["java", "-jar", "backend.jar"]