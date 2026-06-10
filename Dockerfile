# ──────────────────────────────────────────────────────────────────────
# Stage 1: Build
# ──────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Maven wrapper and pom first (layer caching for dependencies)
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ──────────────────────────────────────────────────────────────────────
# Stage 2: Runtime
# ──────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create non-root user
RUN groupadd -r selecta && useradd -r -g selecta selecta

# Copy the built jar
COPY --from=builder /app/target/*.jar app.jar

# Create directory for file uploads
RUN mkdir -p /app/uploads && chown -R selecta:selecta /app

USER selecta

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
