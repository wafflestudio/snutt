#!/usr/bin/env bash
# docker build 로그에서 시간 정보를 뽑아 markdown 요약을 stdout 으로 출력한다.
# GitHub Actions 의 $GITHUB_STEP_SUMMARY 에 붙여 쓰는 것을 전제로 한다.
#
# 사용법: .github/scripts/summarize-docker-build.sh <docker-build-log>
#
# 뽑아내는 것:
#   1. BuildKit 레이어별 소요 시간 (베이스 이미지 pull / COPY / 각 RUN)
#   2. gradle/build-phase.sh 가 남긴 phase 요약
#   3. gradle/build-timing.init.gradle 이 남긴 태스크별 소요 시간
#   4. GraalVM native-image 자체 리포트 (빌드에 할당된 자원, 단계별 비중)

set -euo pipefail

log_file="${1:-}"
if [ -z "$log_file" ] || [ ! -f "$log_file" ]; then
    echo "> docker build 로그를 찾을 수 없어 타이밍 요약을 건너뜁니다. (${log_file:-<none>})"
    exit 0
fi

# BuildKit 은 RUN 의 stdout 에도 "#12 5.432 " 같은 접두사를 붙인다. 내용 매칭 전에 벗겨낸다.
stripped=$(mktemp)
trap 'rm -f "$stripped"' EXIT
sed -E 's/^#[0-9]+ ([0-9]+\.[0-9]+ )?//' "$log_file" > "$stripped"

echo "### 1. Docker 레이어별 소요 시간"
echo ""
layer_rows=$(
    awk '
        match($0, /^#[0-9]+ /) {
            id = substr($0, 2, RLENGTH - 2)
            rest = substr($0, RLENGTH + 1)

            if (rest ~ /^DONE [0-9]/) {
                split(rest, parts, " ")
                seconds = parts[2]
                sub(/s$/, "", seconds)
                printf "%s\t%s\n", seconds, (id in layer_name ? layer_name[id] : "#" id)
                next
            }
            if (rest ~ /^CACHED/) {
                printf "0.0\t%s (CACHED)\n", (id in layer_name ? layer_name[id] : "#" id)
                next
            }
            # 첫 줄이 그 레이어의 설명이다. 진행률/출력 줄은 이름으로 쓰지 않는다.
            if (!(id in layer_name) && rest !~ /^([0-9]+\.[0-9]+ |ERROR|CANCELED|WARN)/) {
                layer_name[id] = rest
            }
        }
    ' "$log_file" | sort -rn | head -25 || true
)

if [ -z "$layer_rows" ]; then
    echo "> BuildKit 레이어 정보를 찾지 못했습니다. \`docker build --progress=plain\` 으로 실행했는지 확인하세요."
else
    echo "| 소요 시간 | 레이어 |"
    echo "| ---: | --- |"
    printf '%s\n' "$layer_rows" | while IFS=$'\t' read -r seconds name; do
        # markdown 표가 깨지지 않도록 파이프를 이스케이프하고 길이를 자른다.
        name=${name//|/\\|}
        if [ "${#name}" -gt 110 ]; then
            name="${name:0:110}…"
        fi
        printf '| %ss | `%s` |\n' "$seconds" "$name"
    done
fi
echo ""

emit_block() {
    local heading="$1"
    local content="$2"
    local empty_note="$3"

    echo "### $heading"
    echo ""
    if [ -z "$content" ]; then
        echo "> $empty_note"
    else
        echo '```'
        printf '%s\n' "$content"
        echo '```'
    fi
    echo ""
}

phase_summary=$(sed -n '/DOCKER BUILD PHASE SUMMARY/,/^=\{20,\}$/p' "$stripped" | head -40 || true)
emit_block "2. Gradle phase 별 소요 시간" "$phase_summary" \
    "phase 요약이 없습니다. Dockerfile 이 gradle/build-phase.sh 를 쓰는지 확인하세요."

task_timing=$(sed -n '/^BUILD TIMING \[/,/^=\{20,\}$/p' "$stripped" | head -200 || true)
emit_block "3. Gradle 태스크별 소요 시간" "$task_timing" \
    "태스크 타이밍이 없습니다. gradle/build-timing.init.gradle 이 적용됐는지 확인하세요."

# GraalVM native-image 는 마지막에 단계별 비중과 사용 자원을 표로 찍는다.
# 러너의 메모리/CPU 가 부족하면 여기서 바로 드러난다.
native_report=$(
    grep -E \
        -e '^ ?- [0-9.]+GB of memory' \
        -e '^ ?- [0-9]+ thread\(s\)' \
        -e '^ ?- [0-9.]+GB of (memory|disk)' \
        -e '\([0-9]+\.[0-9]%\) (Classlist|Setup|Analysis|Universe|Building|Parsing|Inlining|Compiling|Layouting|Image|Write|Total)' \
        -e '^Finished generating' \
        -e 'Peak RSS' \
        -e 'GCs' \
        "$stripped" | head -60 || true
)
emit_block "4. GraalVM native-image 리포트" "$native_report" \
    "native-image 리포트를 찾지 못했습니다. (native 빌드가 아니거나 캐시된 레이어일 수 있습니다.)"
