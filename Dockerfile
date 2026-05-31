# --- Stage 1: Build the application ---
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Install dos2unix to ensure mvnw line endings are compatible with Linux
RUN apk add --no-cache dos2unix

# Copy the maven wrapper and pom.xml first to leverage Docker layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN dos2unix mvnw && chmod +x mvnw

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy the source code and build the application
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# --- Stage 2: Create the runtime image ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a system group and user to run the application as non-root
RUN addgroup -S spring && adduser -S spring -G spring

# Create directories for persistent database and file uploads, then set ownership
RUN mkdir -p /app/data /app/file-storage/subject_resources && \
    chown -R spring:spring /app

# Switch to the non-root user
USER spring:spring

# Copy the packaged JAR file from the builder stage
COPY --from=builder --chown=spring:spring /build/target/*.jar app.jar

# Define environment variables with production-ready defaults
ENV SERVER_PORT=8080 \
    SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/selecta;AUTO_SERVER=TRUE \
    APP_STORAGE_PATH=/app/file-storage/subject_resources/

# Expose the application port
EXPOSE 8080

# Expose volumes for database and uploaded file persistence
VOLUME ["/app/data", "/app/file-storage"]

# Run the application using shell form to dynamically bind to the PORT environment variable (required by Render)
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]

