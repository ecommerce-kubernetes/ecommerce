#!/bin/bash

set -a 
. ./scripts/.env
set +a

MODE=$1
SCRIPT=${2:-auth/login-test.js}
FULL_PATH="scripts/$SCRIPT"

if [ ! -f "$FULL_PATH" ]; then
    echo "파일이 존재하지 않습니다: $FULL_PATH"
    exit 1
fi

if [ "$MODE" = "prom" ]; then
    echo "Prometheus 지표 연동 실행 : $FULL_PATH"
    k6 run -o experimental-prometheus-rw "$FULL_PATH"
else 
    echo "일반 테스트 진행 (HTML 결과만 생성): $FULL_PATHH"
    k6 run "$FULL_PATH"
fi