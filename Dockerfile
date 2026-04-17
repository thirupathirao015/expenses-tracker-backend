# Use Eclipse Temurin JDK 17
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (this layer will be cached)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the port (Railway will override with PORT env var)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "target/expenses-tracker-backend-1.0.0.jar"]
