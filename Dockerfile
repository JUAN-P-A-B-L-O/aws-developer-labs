FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

COPY --from=build --chown=10001:10001 /build/target/aws-developer-labs-*.jar app.jar

USER 10001:10001
ENV SERVER_PORT=8082
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
