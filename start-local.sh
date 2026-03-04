#!/bin/bash
echo "[0/4] 마이크로 서비스 빌드"
SERVICES=(config-service discovery-service gateway-service user-service order-service product-service)

for SERVICE in "${SERVICES[@]}"; do
    echo "빌드 중: $SERVICE"
    cd "$SERVICE" || exit
    chmod +x gradlew

    if grep -q "asciidoctor" build.gradle; then
        echo " asciidoctor 작업 제외하고 빌드 실행"
        ./gradlew clean build -x test -x asciidoctor
    else
        echo "빌드 실행"
        ./gradlew clean build -x test
    fi

    cd ..
done

echo "[1/4] 도커 네트워크 생성"
docker network inspect buynest-network >/dev/null 2>&1 || \
    docker network create buynest-network

echo "[2/4]데이터베이스 컨테이너 실행"
docker-compose -f ./infra/database/docker-compose.yml up -d

echo "[3/4] 미들웨어 컨테이너 실행"
docker-compose -f ./infra/middleware/docker-compose.yml up -d

echo "미들웨어 초기화"
sleep 10

echo "[4/4] 마이크로 서비스 도커 빌드 실행"
docker compose -f docker-compose-local.yml build --progress=plain
docker compose -f docker-compose-local.yml up -d

echo "모든 작업 완료"