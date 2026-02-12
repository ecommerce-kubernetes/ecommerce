---
id: intro
title : 프로젝트 개요
sidebar_position: 1
slug: /intro
---

# 📘 BuyNest: MSA 이커머스 플랫폼

## 프로젝트 소개 

<div class="base-text">
**BuyNest** 는 MSA 기반으로 구성된 **이커머스 시스템** 입니다.

각 서비스들은 **Spring Cloud** 기반의 마이크로 서비스 아키텍쳐로 구성되어 있으며 
서비스간 비동기 통신은 이벤트 스트리밍 기반으로 구현되어 있습니다.

또한 **Eureka** 기반의 서비스 디스커버리,
**Config Server** 를 통한 중앙 설정 관리,
**Gateway**를 활용한 API 라우팅을 적용하여 실제 프로덕션 환경을 고려한 MSA 설계 및 운영 구조를 구현한 프로젝트 입니다.
</div>

## 기술 스택

**☕ Languages & Frameworks**
<div className="tech-stack-wrapper">
  <img src="https://img.shields.io/badge/Java 17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring Boot 3.2-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" />
  <img src="https://img.shields.io/badge/QueryDSL-007ACC?style=for-the-badge&logo=hibernate&logoColor=white" />
</div>

**☁️ MSA & Infrastructure**
<div className="tech-stack-wrapper">
  <img src="https://img.shields.io/badge/Spring Cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring Cloud Gateway-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/Netflix Eureka-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
</div>

**💾 Data & Messaging**

<div className="tech-stack-wrapper">
  <img src="https://img.shields.io/badge/Apache Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL 8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" />
</div>

## 시스템 구조 (System Architecture)

<div class="base-text">
시스템 구조는 **Spring Cloud** 기반 마이크로 서비스 아키텍쳐로 구성되어 있습니다.
전체 시스템은 **Gateway**, **Management**, **Business**, **Data** 4가지 계층으로 구성되어있습니다.
</div>

* **Entry & Routing**: 모든 클라이언트 요청은 **API GATEWAY** 를 통해 단일 진입점으로 처리되며, 이곳에서 JWT 토큰 인증 및 각 서비스로의 라우팅을 수행합니다.
* **Management Layer**: **Eureka**와 **Config Service**를 통해 마이크로 서비스 인스턴스의 위치와 설정 정보를 중앙에서 관리합니다.
* **Event-Driven Core**: 마이크로 서비스간의 결합도를 낮추기위해 **Kafka**기반 비동기 이벤트 통신 구조를 적용했습니다.
  주문 발생시 주문 서비스가 주문 생성 이벤트를 발행하면 상품 서비스와 유저서비스가 이벤트를 수신해 각자의 트랜잭션 범위에서 처리하도록 설계하였습니다.
* **Persistence**: 각 마이크로 서비스는 독립적인 데이터베이스를 가지며 유저 서비스는 안전한 토큰 관리와 만료 처리를 위해 **Redis**를 저장소로 활용했습니다.

### 시스템 구조도

<div style={{ display: 'flex', justifyContent: 'center' }}>
  <img src="/img/system_architecture.png" alt="아키텍처" width="700" />
</div>

## CICD (배포 CICD 파이프라인)

<div class="base-text">
 개발 생산성과 안정적인 배포를 위해 **Jenkins**, **Ansible**, **Docker**를 활용한 자동화 파이프라인을 구축했습니다.
 소스코드 변경 사항은 **Github Webhook**을 통해 실시간으로 감지되며 빌드, 배포를 수행했습니다.
</div>

### 배포 프로세스
1. **Code Push & Trigger** : 깃허브에 코드를 Push 하면 Webhook이 트리거 되어 **Jenkins**가 빌드 작업을 수행합니다.
2. **Build & Artifacts** : Jenkins는 애플리케이션을 빌드 하고 이를 **Docker Image**로 패키징 합니다.
3. **Push to Registry** : 생성된 도커 이미지는 **Docker Hub**에 업로드 됩니다.
4. **Infrastructure as Code** : Jenkins는 **Ansible**에게 배포 명령을 전달하면 **Ansible**은 Playbook을 실행합니다.
5. **Deploy**: 운영 서버는 Docker Hub에서 최신 이미지를 **Pull** 받아 컨테이너를 교체해 배포를 완료 합니다.

### CICD 구조도

<div style={{ display: 'flex', justifyContent: 'center' }}>
  <img src="/img/cicd.png" alt="cicd" width="700" />
</div>

## 주요 기능

<div className="full-width-table">
| 서비스 (Service)                 | 핵심 역할 (Core Domain) | 구현 상세                                                                                                                                               |
|:------------------------------|:--------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------|
| **API Gateway**               | 라우팅/인증              | • **Spring Cloud Gateway**: Netty 기반의 Non-blocking I/O 아키텍처<br/>• **JWT Filter**: 인증 필터를 통한 토큰 검증 및 파싱<br/>• **Load Balancing**: 라운드 로빈 방식의 서비스 라우팅 |
| **Discovery Service(Eureka)** | 서비스 등록              | • **Netflix Eureka**: 마이크로 서비스 인스턴스의 상태(Up/Down) 감지<br/>• **Heartbeat**: 주기적인 헬스 체크를 통해 가용성 없는 인스턴스 자동 제거                                           |
| **Config Service**            | 설정 중앙화              | • **Spring Cloud Config**: Git 저장소를 백엔드로 하여 설정 이력 관리<br/>• **Cipher Encryption**: DB 비밀번호 등 민감 정보를 암호화(`{cipher}...`)하여 저장                          |
| **User Service**              | 회원 관련               | • **Spring Security**: JWT 기반 인증 아키텍처<br/>• **Redis**: Refresh Token 저장소                                                                            | 
| **Product Service**           | 상품 관련               | • **Spring Security**: 커스텀 헤더 기반 인증 아키텍처<br/>• 상품 관련 엔티티 저장 및 조회, 수정                                                                                |
| **Order Service**             | 주문 관련               | • **Event-Driven**: Kafka를 활용한 비동기 주문 생성<br/>• **SAGA Pattern**: 분산 트랜잭션의 데이터 정합성 보장 (유저 포인트/재고 보상 트랜잭션)                                            |
</div>

## Service Communication Strategy

<div style={{ display: 'flex', gap: '20px', flexDirection: 'column' }}>
  <div style={{ padding: '15px', border: '1px solid #ddd', borderRadius: '8px', backgroundColor: '#f9f9f9' }}>
    <span style={{ fontSize: '20px', fontWeight: 'bold', color: '#E8590C' }}>
      🔹 Kafka 기반 비동기 이벤트 처리
    </span>
    <p style={{ margin: '10px 0 0 0', lineHeight: '1.6' }}>
      이벤트 기반 아키텍처(EDA)를 도입하여 <strong>서비스 간 결합도를 낮추었습니다.</strong><br/>
      특히 SAGA 패턴을 적용하여 분산 환경에서의 데이터 정합성을 보장하고, 대량의 트래픽을 <strong>비동기로 처리(Non-blocking)</strong>하여 성능을 최적화했습니다.
    </p>
  </div>

  <div style={{ padding: '15px', border: '1px solid #ddd', borderRadius: '8px', backgroundColor: '#f9f9f9' }}>
    <span style={{ fontSize: '20px', fontWeight: 'bold', color: '#1098AD' }}>
      🔹 Feign Client 기반 동기 통신
    </span>
    <p style={{ margin: '10px 0 0 0', lineHeight: '1.6' }}>
      즉각적인 응답이 필요한 조회 로직에는 <strong>Feign Client</strong>를 사용했습니다.<br/>
      이때 <strong>Circuit Breaker</strong>를 함께 적용하여, 타 서비스 장애 시 <strong>장애 전파를 차단</strong>하도록 설계했습니다.
    </p>
  </div>
</div>