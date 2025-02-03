FROM openjdk:17-jdk-slim
WORKDIR /app
ARG CODEARTIFACT_AUTH_TOKEN
COPY . /app
RUN ./gradlew :api:bootJar -PcodeArtifactAuthToken=$CODEARTIFACT_AUTH_TOKEN
EXPOSE 8080
ENTRYPOINT java $JAVA_OPTS -jar api/build/libs/snutt-api.jar
