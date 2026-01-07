FROM ghcr.io/graalvm/jdk-community:25
WORKDIR /app
COPY . /app
RUN microdnf install -y findutils --nodocs
RUN --mount=type=secret,id=codeartifact_token ./gradlew :api:bootJar -PcodeArtifactAuthToken=$(cat /run/secrets/codeartifact_token)
EXPOSE 8080
ENTRYPOINT java $JAVA_OPTS -jar api/build/libs/snutt-api.jar
