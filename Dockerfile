
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

ENV TZ=Europe/Moscow

RUN groupadd --system app && useradd --system --gid app --home /app app

RUN mkdir -p /data && chown app:app /data
VOLUME ["/data"]

COPY --from=build --chown=app:app /workspace/build/libs/*.jar app.jar

USER app

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
