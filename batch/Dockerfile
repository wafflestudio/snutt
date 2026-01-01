FROM ghcr.io/graalvm/jdk-community:25
WORKDIR /app
ARG CODEARTIFACT_AUTH_TOKEN
COPY . /app
RUN microdnf install -y findutils --nodocs
RUN ./gradlew :batch:bootJar -PcodeArtifactAuthToken=$CODEARTIFACT_AUTH_TOKEN
ENTRYPOINT java $JAVA_OPTS -jar batch/build/libs/snutt-batch.jar --job.name=${JOB_NAME}
