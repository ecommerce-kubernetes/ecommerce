@echo off
chcp 65001 > nul

echo [0/3] 전체 마이크로 서비스 빌드
call gradlew.bat clean build -x test

echo [1/3] 도커 네트워크 생성
docker network inspect buynest-network >nul 2>&1 || docker network create buynest-network

echo [2/3] 미들웨어 인프라 기동
docker compose -f infra/middleware/docker-compose.yml up -d

echo 미들웨어 초기화 대기 (10초)...
timeout /t 10 /nobreak > nul

echo [3/3] 마이크로 서비스 기동
docker compose -f docker-compose-local.yml up -d --build