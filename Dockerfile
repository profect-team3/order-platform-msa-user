FROM gradle:8.8-jdk17 AS builder

WORKDIR /workspace

COPY gradlew .
COPY gradlew.bat .
COPY gradle ./gradle

COPY build.cloud.gradle build.gradle

COPY src ./src
COPY libs ./libs

RUN ./gradlew bootJar

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar /app/application.jar

EXPOSE 8081

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/application.jar"]
