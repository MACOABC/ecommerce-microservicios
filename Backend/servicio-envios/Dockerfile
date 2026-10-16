# syntax=docker/dockerfile:1
# Contexto: Backend/ (el POM padre y los cuatro módulos deben estar disponibles).
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -pl servicio-envios -am package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN apk add --no-cache curl \
    && addgroup -S app \
    && adduser -S -G app app
WORKDIR /app
COPY --from=build --chown=app:app /build/servicio-envios/target/servicio-envios-1.0.0.jar app.jar
USER app
ENV BIND_ADDRESS=0.0.0.0 \
    PORT=8083
EXPOSE 8083
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=6 \
    CMD curl --fail --silent --max-time 4 http://127.0.0.1:8083/actuator/health > /dev/null || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
