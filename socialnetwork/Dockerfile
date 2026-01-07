# Multi-stage Dockerfile for building and running the Spring Boot application
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# copy maven files and source
COPY pom.xml ./
COPY src ./src

# build the application (skip tests for faster image builds)
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

# copy built jar from the build stage (wildcard handles snapshot or versioned jar)
COPY --from=build /workspace/target/*.jar app.jar

# allow passing java options via environment and default to port 80
ENV JAVA_OPTS="-Dserver.port=80"

EXPOSE 80

# run the app (honor `JAVA_OPTS` for additional JVM args)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
