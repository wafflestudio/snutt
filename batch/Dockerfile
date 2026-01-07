FROM ghcr.io/graalvm/jdk-community:25
WORKDIR /app
COPY . /app
RUN microdnf install -y findutils --nodocs
RUN --mount=type=secret,id=codeartifact_token ./gradlew :batch:bootJar -PcodeArtifactAuthToken=$(cat /run/secrets/codeartifact_token)
ENTRYPOINT java $JAVA_OPTS -jar batch/build/libs/snutt-batch.jar --job.name=${JOB_NAME}
