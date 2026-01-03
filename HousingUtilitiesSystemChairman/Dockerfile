# ==============================================================================
# Multi-stage Dockerfile for HousingUtilitiesSystemChairman
# Optimized for CI/CD with pre-built JAR from Maven
# ==============================================================================

# Runtime stage
FROM eclipse-temurin:17-jre-alpine AS runtime

# Labels
LABEL maintainer="HousingUtilitiesSystem Team"
LABEL description="Housing Utilities System - Chairman Module"

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Create application directories
RUN mkdir -p /app/uploads /app/temp-uploads /app/logs && \
    chown -R appuser:appgroup /app

WORKDIR /app

# Copy the pre-built JAR from target directory
COPY target/HousingUtilitiesSystemChairman-0.0.1-SNAPSHOT.jar app.jar

# Set ownership
RUN chown appuser:appgroup app.jar

# Switch to non-root user
USER appuser

# Expose port (Chairman runs on 8082)
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# JVM optimizations for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=docker"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
