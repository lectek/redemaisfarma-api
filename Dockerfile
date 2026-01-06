# syntax=docker/dockerfile:1.5

# ========================
# BUILD STAGE
# ========================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /workspace
ENV SPRING_PROFILES_ACTIVE=docker

# Copy POM and Maven Wrapper (dependency cache)
COPY pom.xml mvnw* ./
COPY .mvn .mvn

# Dependency cache
RUN mvn -B -ntp dependency:go-offline

# Copy source
COPY src src

# Package jar (skip tests)
RUN mvn -B -ntp -DskipTests=true -DskipITs=true package

# Extract layers for faster rebuilds
RUN java -Djarmode=layertools -jar /workspace/target/*.jar extract --destination /workspace/target/layers

# ========================
# RUNTIME STAGE
# ========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Install curl and certificates for healthchecks/debug (optional)
ARG INSTALL_DEBUG_TOOLS=false
USER root
RUN if [ "$INSTALL_DEBUG_TOOLS" = "true" ]; then \
      apt-get update \
      && apt-get install -y --no-install-recommends curl ca-certificates \
      && rm -rf /var/lib/apt/lists/*; \
    fi

# Copy layered artifacts
COPY --from=builder /workspace/target/layers/dependencies/ /app/
COPY --from=builder /workspace/target/layers/spring-boot-loader/ /app/
COPY --from=builder /workspace/target/layers/snapshot-dependencies/ /app/
COPY --from=builder /workspace/target/layers/application/ /app/

# Optional non-root user
RUN useradd -r -u 1001 -g root appuser \
 && chown -R appuser:root /app
USER appuser

# Memory and encoding
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

# Enable ANSI logs and profiles via env
ENV SPRING_OPTS="--spring.output.ansi.enabled=ALWAYS --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"

EXPOSE 8080

ENTRYPOINT ["sh", "-lc", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher $SPRING_OPTS"]
