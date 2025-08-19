# Stage 1: Build
FROM gradle:8.8-jdk17 AS builder
WORKDIR /workspace

# 루트 프로젝트 전체 복사
COPY gradlew gradlew.bat /workspace/
COPY gradle /workspace/gradle
COPY settings.gradle* build.gradle* gradle.properties* /workspace/
COPY . /workspace/

# Gradle 실행권한
RUN chmod +x ./gradlew

# 빌드 (테스트 제외)
RUN ./gradlew :order-platform-msa-user:build -x test

# Stage 2: 런타임 이미지
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# User 서비스 jar만 복사
COPY --from=builder /workspace/order-platform-msa-user/build/libs/*.jar /app/application.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
