FROM gradle:8.8-jdk17 AS builder
WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle

COPY order-platform-msa-user ./order-platform-msa-user

RUN ./gradlew :order-platform-msa-user:build -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /workspace/order-platform-msa-user/build/libs/*.jar /app/application.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
