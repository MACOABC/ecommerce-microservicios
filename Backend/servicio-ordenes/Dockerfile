# syntax=docker/dockerfile:1
# Contexto: Backend/ (el POM padre y los cuatro módulos deben estar disponibles).
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -pl servicio-ordenes -am package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN apk add --no-cache curl \
    && addgroup -S app \
    && adduser -S -G app app
WORKDIR /app
COPY --from=build --chown=app:app /build/servicio-ordenes/target/servicio-ordenes-1.0.0.jar app.jar
USER app
ENV BIND_ADDRESS=0.0.0.0 \
    PORT=8082
EXPOSE 8082
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=6 \
    CMD curl --fail --silent --max-time 4 http://127.0.0.1:8082/actuator/health > /dev/null || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
