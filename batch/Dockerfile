FROM ghcr.io/graalvm/jdk-community:21
WORKDIR /app
ARG CODEARTIFACT_AUTH_TOKEN
COPY . /app
RUN ./gradlew :batch:bootJar -PcodeArtifactAuthToken=$CODEARTIFACT_AUTH_TOKEN
ENTRYPOINT java $JAVA_OPTS -jar batch/build/libs/snutt-batch.jar --job.name=${JOB_NAME}
