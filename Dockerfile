FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . /app
RUN ./gradlew :api:bootJar
EXPOSE 8080
ENTRYPOINT java $JAVA_OPTS -jar api/build/libs/snu4t-api.jar
