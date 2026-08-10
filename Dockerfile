FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/
RUN chmod +x gradlew
COPY src/ src/
RUN ./gradlew clean test bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/cache-strategies-bench-*.jar /app/app.jar
RUN mkdir -p /app/benchmarks/results
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
