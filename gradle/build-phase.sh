#!/bin/sh
# Docker 빌드 안에서 gradle 을 "phase" 단위로 실행하면서 각 phase 의 소요 시간을 기록한다.
# native image 빌드가 오래 걸릴 때, 의존성 해석/컴파일/AOT/native-image 중 어디가 긴지
# CI 로그만 보고 판단하기 위한 것이다.
#
# 사용법:
#   sh gradle/build-phase.sh <phase-name> <gradle-args...>
#   sh gradle/build-phase.sh --summary
#
# 환경변수:
#   BUILD_PHASE_TIMING_FILE   phase 기록 파일 경로 (기본 build/phase-timings.tsv)
#
# POSIX sh 로만 작성한다. 빌더 이미지(oraclelinux 기반)에 awk 등이 없을 수 있다.

set -eu

timing_file="${BUILD_PHASE_TIMING_FILE:-build/phase-timings.tsv}"

format_duration() {
    seconds="$1"
    minutes=$((seconds / 60))
    if [ "$minutes" -gt 0 ]; then
        printf '%dm %02ds' "$minutes" "$((seconds % 60))"
    else
        printf '%ds' "$seconds"
    fi
}

print_summary() {
    if [ ! -f "$timing_file" ]; then
        echo "[phase] 기록된 phase 가 없습니다: $timing_file"
        return 0
    fi

    total=0
    while IFS='	' read -r seconds name; do
        total=$((total + seconds))
    done < "$timing_file"

    divisor="$total"
    if [ "$divisor" -le 0 ]; then
        divisor=1
    fi

    echo ""
    echo "==================== DOCKER BUILD PHASE SUMMARY ===================="
    while IFS='	' read -r seconds name; do
        printf '  %10s  %3d%%  %s\n' "$(format_duration "$seconds")" "$((seconds * 100 / divisor))" "$name"
    done < "$timing_file"
    printf '  %10s  %3d%%  %s\n' "$(format_duration "$total")" 100 "TOTAL (gradle phases)"
    echo "===================================================================="
    echo ""
}

if [ "$#" -eq 0 ]; then
    echo "usage: sh gradle/build-phase.sh <phase-name> <gradle-args...> | --summary" >&2
    exit 2
fi

if [ "$1" = "--summary" ]; then
    print_summary
    exit 0
fi

phase="$1"
shift

mkdir -p "$(dirname "$timing_file")"

BUILD_TIMING_LABEL="$phase"
export BUILD_TIMING_LABEL

started_at=$(date +%s)
echo "[phase] >>> START  ${phase}  ($(date -u '+%Y-%m-%dT%H:%M:%SZ'))"

./gradlew "$@" --console=plain --init-script gradle/build-timing.init.gradle

elapsed=$(($(date +%s) - started_at))
printf '%s\t%s\n' "$elapsed" "$phase" >> "$timing_file"
echo "[phase] <<< DONE   ${phase}  ($(format_duration "$elapsed"))"
