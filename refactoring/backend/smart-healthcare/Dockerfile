# Stage 1: Build
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Gradle 래퍼 및 빌드 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .

# 의존성 다운로드 (캐싱 최적화)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN gradle bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 타임존 설정
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 실행
EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dspring.profiles.active=prod"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

