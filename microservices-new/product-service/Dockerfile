FROM eclipse-temurin:21-jdk-alpine

COPY target/*.jar app.jar
COPY src/main/resources/application-docker.properties application.properties

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]