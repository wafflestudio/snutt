# 빌드 소요 시간 계측

이 프로젝트는 런타임 메모리 사용량을 줄이기 위해 GraalVM native image 로 빌드한다.
대신 빌드가 오래 걸리기 때문에, **어느 단계에서 시간이 드는지** CI 로그만 보고 알 수 있도록
계측을 붙여 두었다.

## 어디를 보면 되나

배포 워크플로우(`_deploy.yml`, `_deploy-native.yml`)가 끝나면 **Actions 실행 화면의 Summary**에
4단계 요약이 붙는다.

| 섹션 | 알 수 있는 것 |
| --- | --- |
| 1. Docker 레이어별 소요 시간 | 베이스 이미지 pull / 빌드 컨텍스트 전송 / 각 RUN / 이미지 export 중 무엇이 긴지 |
| 2. Gradle phase 별 소요 시간 | `compile+aot` 와 `nativeCompile` 의 비중 |
| 3. Gradle 태스크별 소요 시간 | 설정·의존성 해석 시간, 그리고 개별 태스크(`compileKotlin`, `processAot`, `nativeCompile` …) 소요 시간 |
| 4. GraalVM native-image 리포트 | native-image 에 할당된 메모리/스레드, 단계별(Analysis / Compiling code / …) 비중, Peak RSS |

Summary 위쪽에는 `docker build` 와 `docker push` 각각의 소요 시간과 이미지 크기도 함께 찍힌다.
원본 로그 전체는 같은 실행의 아티팩트 `docker-build-log-*` 로 7일간 남는다.

특히 4번 섹션의 **"Build resources"** 줄을 눈여겨볼 것. GitHub 러너의 메모리가 부족하면
native-image 가 힙을 좁게 잡고 GC 를 반복하면서 빌드 시간이 몇 배로 늘어난다.
`Peak RSS` 와 `GC: in Xs (Y% of total time)` 이 그 신호다.

## 구성 요소

| 파일 | 역할 |
| --- | --- |
| `gradle/build-timing.init.gradle` | Gradle init script. 태스크별 소요 시간과 "첫 태스크 시작 전까지 걸린 시간"(= 설정 + 의존성 해석)을 빌드 끝에 표로 출력한다. |
| `gradle/build-phase.sh` | Docker 빌드 안에서 gradle 을 phase 단위로 실행하고 각 phase 의 소요 시간을 누적 기록한다. `--summary` 로 최종 표를 출력한다. |
| `.github/scripts/summarize-docker-build.sh` | `docker build` 로그에서 위 정보들을 추출해 markdown 요약을 만든다. |

native Dockerfile 은 빌드를 두 phase 로 나눠 놓았다.

- `compile+aot` — 의존성 해석/다운로드, Kotlin 컴파일, Spring AOT 처리, AOT 생성 코드 컴파일 (`:api:aotClasses`)
- `nativeCompile` — GraalVM native-image 컴파일 (`:api:nativeCompile`)

경계를 `aotClasses` 로 잡은 이유는, `processAot` 로 끊으면 AOT 가 생성한 소스의 컴파일이
`nativeCompile` phase 로 넘어가 native-image 자체의 시간이 부풀려 보이기 때문이다.

## 로컬에서 돌려보기

태스크별 소요 시간만 보고 싶을 때:

```bash
./gradlew :api:nativeCompile --console=plain --init-script gradle/build-timing.init.gradle
```

CI 와 같은 phase 단위로 보고 싶을 때:

```bash
sh gradle/build-phase.sh "api:compile+aot" :api:aotClasses
sh gradle/build-phase.sh "api:nativeCompile" :api:nativeCompile
sh gradle/build-phase.sh --summary
```

Docker 빌드 로그를 직접 요약해보고 싶을 때:

```bash
docker build --progress=plain -f api/Dockerfile-native . 2>&1 | tee /tmp/docker-build.log
.github/scripts/summarize-docker-build.sh /tmp/docker-build.log
```

### 환경변수

| 이름 | 기본값 | 설명 |
| --- | --- | --- |
| `BUILD_TIMING_LABEL` | `gradle` | 리포트 헤더 라벨. 한 빌드에서 gradle 을 여러 번 호출할 때 구분용. |
| `BUILD_TIMING_MIN_MILLIS` | `500` | 이 시간 미만인 태스크는 개별 행으로 출력하지 않고 합산한다. |
| `BUILD_TIMING_OUTPUT` | (없음) | 지정하면 `소요시간(ms)<TAB>결과<TAB>태스크` TSV 를 해당 경로에 추가로 기록한다. |
| `BUILD_PHASE_TIMING_FILE` | `build/phase-timings.tsv` | `build-phase.sh` 가 phase 기록을 쌓는 파일. |

## 알아둘 것

- 태스크가 병렬로 실행되면 "task time (sum)" 이 "task execution window" 보다 클 수 있다.
  둘 다 찍는 이유가 이것이다.
- phase 를 나누면서 gradle 을 두 번 호출하므로, 두 번째 호출의 설정 단계(수십 초)가 추가로 든다.
  대신 native-image 시간이 다른 작업과 섞이지 않고 분리되어 보인다. 계측을 위해 치르는 비용이다.
- 지금 Docker 빌드에는 Gradle 캐시가 없다. 매 빌드가 의존성을 새로 받는다.
  3번 섹션의 "configuration + dependency resolution" 이 그 비용이며,
  이 값이 크게 나온다면 캐시 도입을 검토할 근거가 된다.
