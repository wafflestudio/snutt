FROM ghcr.io/graalvm/jdk-community:25
WORKDIR /app
COPY . /app
RUN microdnf install -y findutils --nodocs
# native 빌드와 같은 계측을 붙여 둔다. JVM 빌드 시간이 native 빌드 시간의 기준선이 된다.
RUN --mount=type=secret,id=github_token \
    GITHUB_TOKEN=$(cat /run/secrets/github_token) \
    sh gradle/build-phase.sh "batch:bootJar" :batch:bootJar
RUN sh gradle/build-phase.sh --summary
ENTRYPOINT java $JAVA_OPTS -jar batch/build/libs/snutt-batch.jar --job.name=${JOB_NAME}
