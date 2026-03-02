#!/bin/bash

set -a 
. ./scripts/.env
set +a

MODE=$1
SCRIPT=${2:-login-test.js}

if [ ! -f "scripts/$SCRIPT" ]; then
    echo "파일이 존재하지 않습니다: scripts/$SCRIPT"
    exit 1
fi

if [ "$MODE" == "prom"]; then
    echo "Prometheus 지표 연동 실행 : scripts/$SCRIPT"
    k6 run -o experimental-prometheus-rw "scripts/$SCRIPT"
else 
    echo "일반 테스트 진행 (HTML 결과만 생성): scripts/$SCRIPT"
    k6 run "scripts/$SCRIPT"
fi