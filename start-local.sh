#!/bin/bash
echo "[1/3] 도커 네트워크 생성"
docker network inspect buynest-network >/dev/null 2>&1 || \
    docker network create buynest-network

echo "[2/3] 미들웨어 인프라 기동"
docker compose -f infra/middleware/docker-compose.yml up -d 

echo "미들웨어 초기화"
sleep 10

echo "[3/3] 마이크로 서비스 기동"
docker-compose -f docker-compose-local.yml up -d --build