# ==========================================
# Stage 1: Build JAR with Maven & Temurin JDK 21
# ==========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Minimal Runtime with Temurin JRE 21
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install wget for Docker healthcheck
RUN apk add --no-cache wget

# Copy executable jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
