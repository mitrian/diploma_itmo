FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle gradle
COPY app app
COPY auth auth
COPY voting voting
COPY history history
RUN chmod +x gradlew \
  && ./gradlew :app:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd -r -g 1001 app && useradd -r -u 1001 -g app app
COPY --from=builder /workspace/app/build/libs/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
