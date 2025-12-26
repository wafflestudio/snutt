FROM ghcr.io/graalvm/jdk-community:25
WORKDIR /app
ARG CODEARTIFACT_AUTH_TOKEN
COPY . /app
RUN microdnf install -y findutils --nodocs
RUN ./gradlew :api:bootJar -PcodeArtifactAuthToken=$CODEARTIFACT_AUTH_TOKEN
EXPOSE 8080
ENTRYPOINT java $JAVA_OPTS -jar api/build/libs/snutt-api.jar
