# Build stage: compile and package the Spring Boot jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src src
RUN mvn -B -q package -DskipTests

# Runtime stage: slim JRE image, running as a non-root user
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app \
    && mkdir -p /data/uploads && chown -R app:app /data
COPY --from=build /app/target/recruitment-1.0.0.jar app.jar
USER app
ENV UPLOADS_DIR=/data/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
