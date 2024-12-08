# OpenJDK 17 이미지를 기반으로 설정
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# Gradle wrapper 파일과 소스 파일을 복사
COPY gradlew /app/gradlew
COPY gradle /app/gradle
COPY build.gradle /app/build.gradle
COPY src /app/src

# Gradle wrapper를 실행하여 의존성 다운로드 및 빌드
RUN ./gradlew build

# JUnit 테스트를 실행하도록 설정
CMD ["./gradlew", "test"]

