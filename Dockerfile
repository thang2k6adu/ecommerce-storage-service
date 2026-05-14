FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests \
    && find target -maxdepth 1 -type f -name "*.jar" ! -name "*.original" -exec cp {} /workspace/app.jar \;

FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

RUN useradd --system --create-home --uid 1001 appuser \
    && mkdir -p /app/data/storage \
    && chown -R appuser:appuser /app/data

USER appuser

COPY --from=builder /workspace/app.jar ./app.jar

EXPOSE 8087
ENV JAVA_OPTS=""
ENV STORAGE_LOCAL_ROOT=/app/data/storage
ENV STORAGE_PUBLIC_BASE_URL=http://localhost:8087/api/storage/files

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
