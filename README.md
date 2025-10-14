# 🛒 Buynest [E-Commerce Microservices Platform] 

> Spring Boot 기반 **전자상거래 마이크로서비스 프로젝트**입니다.  
> 서비스 간 비동기 이벤트 통신(Kafka)과 Eureka 기반 서비스 디스커버리,  
> Config Server를 이용한 중앙 설정 관리, Gateway를 통한 API 라우팅을 구현했습니다.  
> 실제 프로덕션 환경 수준의 **MSA 설계 능력과 운영 구조 설계 경험**을 보여주기 위한 포트폴리오 프로젝트입니다.

---

## 📐 Architecture Overview

<p align="left">
  <img src="https://github.com/user-attachments/assets/df5a3918-463a-44c5-8c53-7ef7b609a2d2" width="70%"/>
  <br/>
  <em>Jenkins CI/CD Pipeline</em>
</p>

<p align="left">
  <img src="https://github.com/user-attachments/assets/d86536eb-e6be-4169-bd2d-0113d4465005" width="70%" />
  <br/>
</p>

<p align="left">
  <img src="https://github.com/user-attachments/assets/47054e9b-c18e-4869-b232-21545a835fdd" width="70%" />
  <br/>
</p>

<p align="left">
  <img src="https://github.com/user-attachments/assets/d8ccbedd-bc02-41fe-8702-f652cabca638" width="70%" />
  <br/>
  <em>전체 마이크로서비스 구조도</em>
</p>

---

## ⚙️ Services Overview




| Service | Description |
|----------|--------------|
| **User Service** | 회원 관리, 인증 및 권한 처리 |
| **Product Service** | 상품 등록, 수정, 조회 |
| **Order Service** | 주문 생성 및 상태 관리 |
| **Coupon Service** | 쿠폰 발급, 검증, 만료 처리 |
| **Image Service** | 상품 이미지 저장 및 관리 |
| **Config Service** | 중앙 설정 서버 |
| **Discovery Service (Eureka)** | 서비스 등록/탐색 |
| **Gateway Service** | 외부 요청 진입점 (라우팅/보안) |

---

## 🧩 Communication Strategy

### 🔹 Kafka 기반 비동기 이벤트 처리
- 대용량 요청 처리, 결합도 감소
- `order.created`, `order.completed`, `user.point.updated` 등 이벤트 기반 메시징 구조

### 🔹 Feign Client 기반 REST 통신
- 순서가 중요한 트랜잭션(예: 결제 승인 → 주문 확정)에서는 REST 사용
- 장애 상황에서도 **idempotent** 구조로 설계

---

## 🧠 Tech Stack

| Category | Stack |
|-----------|--------|
| **Backend** | Spring Boot 3, Spring Cloud (Eureka, Config, Gateway), Spring Security |
| **Messaging** | Apache Kafka |
| **Database** | MySQL, Redis |
| **Infra** | Docker, Nginx, Kubernetes(구상) |
| **Monitoring** | Zipkin |
| **Build/Deploy** | Jenkins CI/CD (Pipeline 기반), Ansible |
| **Language** | Java 17 |

---

## 🚀 Deployment

> 현재는 실제 클라우드 환경 대신, **하나의 서버에 Docker 컨테이너 형태로 배포**하여 운영 중입니다.  
> Kubernetes 배포 구성을 미리 설계해두었으며, 클라우드 환경 전환 시 손쉽게 확장 가능합니다.

---

## 📦 How to Run (Local)

Kafka, MySQL, Redis, Eureka, Config Server를 먼저 실행해야 합니다.
이후 각 서비스들이 Eureka에 등록되고, Gateway를 통해 접근할 수 있습니다.

---

## 🧭 Event Flow Example

사용자가 POST /orders 요청을 보냄

Order Service → order.created 이벤트 발행

User Service, Product Service, Coupon Service 각각 소비 후 처리

성공 시 order.success 이벤트, 실패 시 order.failed 이벤트 발행

Order Service에서 최종 상태 업데이트

Redis를 이용해 상태 캐싱 및 집계

## 🌐 Domain

현재 실제 배포 서버: https://buynestshop.store

## 👨‍💻 Developer

> 유호연 (Ho-Yeon Yu)
> Email: [zmfmsh46@gmail.com]
> GitHub: https://github.com/zmfmsh46

> 최민식 ()
> Email: []
> GitHub: 


## 📄 License

이 프로젝트는 개인 포트폴리오 용도로 제작되었으며,
상업적 사용은 금지됩니다.
















