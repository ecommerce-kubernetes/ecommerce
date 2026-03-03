@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

echo [0/4] 마이크로 서비스 빌드
set SERVICES=config-service discovery-service, order-service product-service user-service gateway-service

for %%s in (%SERVICES%) do (
    echo.
    echo 빌드 중: %%s
    cd %%s

    findStr "asciidoctor" build.gradle >nul
    if !errorlevel! equ 0 (
        echo - asciidoctor 작업 제외하고 빌드 실행
        call gradlew.bat clean build -x test -x asciidoctor
    ) else (
        echo - 빌드 실행
        call gradlew.bat clean build -x test
    )
    
    cd ..
)

echo.
echo [1/4] 도커 네트워크 생성
docker network inspect buynest-network >nul 2>&1 || docker network create buynest-network

echo [2/4] 데이터베이스 컨테이너 실행
docker compose -f ./infra/database/docker-compose.yml up -d

echo [3/4] 미들웨어 컨테이너 실행 
docker compose -f ./infra/middleware/docker-compose.yml up -d 

echo 미들웨어 초기화 대기
timeout /t 10 /nobreak > nul

echo [4/4] 마이크로 서비스 도커 빌드 실행
docker compose -f docker-compose-local.yml up -d --build

echo 모든 작업 완료