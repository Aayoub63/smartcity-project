# Étape 1 : Compilation du code (Build) avec Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image finale légère pour l'exécution (Run) compatible Mac M1/M2/M3
FROM eclipse-temurin:17-jdk-jammy
VOLUME /tmp
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
