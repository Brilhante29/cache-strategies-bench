FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle/ gradle/
COPY src/ src/
RUN gradle build --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/cache-strategies-bench-*.jar /app/app.jar
RUN mkdir -p /app/benchmarks/results
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
