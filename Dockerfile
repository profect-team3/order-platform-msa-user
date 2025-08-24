FROM gradle:8.8-jdk17 AS builder
WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle ./
COPY gradle ./gradle
COPY order-platform-msa-user ./order-platform-msa-user
COPY order-platform-msa-user/build.cloud.gradle ./order-platform-msa-user/build.gradle

RUN ./gradlew :order-platform-msa-user:bootJar -x test

FROM eclipse-temurin:17-jre-slim
WORKDIR /app

COPY --from=builder /workspace/order-platform-msa-user/build/libs/*-boot.jar /app/application.jar

EXPOSE 8081
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/application.jar"]
