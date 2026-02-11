FROM ghcr.io/graalvm/jdk-community:25
WORKDIR /app
COPY . /app
RUN microdnf install -y findutils --nodocs
RUN --mount=type=secret,id=github_token GITHUB_TOKEN=$(cat /run/secrets/github_token) ./gradlew :api:bootJar
EXPOSE 8080
ENTRYPOINT java $JAVA_OPTS -jar api/build/libs/snutt-api.jar
