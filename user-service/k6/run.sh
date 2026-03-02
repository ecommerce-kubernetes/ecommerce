#!/bin/bash

set -a 
. ./scripts/.env
set +a

MODE=$1
SCRIPT=${2:-auth/login-test.js}
PATH="scripts/$SCRIPT"

if [ ! -f "$PATH" ]; then
    echo "파일이 존재하지 않습니다: $PATH"
    exit 1
fi

if [ "$MODE" == "prom"]; then
    echo "Prometheus 지표 연동 실행 : $PATH"
    k6 run -o experimental-prometheus-rw "$PATH"
else 
    echo "일반 테스트 진행 (HTML 결과만 생성): $PATH"
    k6 run "$PATH"
fi