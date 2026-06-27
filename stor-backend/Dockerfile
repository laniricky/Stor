FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/stor-backend-all.jar stor-backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "stor-backend.jar"]
