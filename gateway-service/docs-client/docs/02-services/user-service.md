---
id: user-service
title: 👤 User Service
sidebar_position: 3
---

# 👤 User Service

<div class="base-text">
  **회원 도메인** 을 담당하는 마이크로 서비스입니다.
  사용자 회원가입 및 로그인과 같은 인증 기능을 처리하며, JWT 기반의 인증/인가를 제공합니다. 
  또한 주문 서비스와의 연동을 위한 **Internal API**를 제공하고 주문 이벤트를 소비하여 포인트 적립 및 차감을 처리하는  
  **Kafka Consumer 기반의 비동기 처리 로직**을 포함하고 있습니다.
</div>

## 🛠️ 기술 스택
<div style={{display: 'flex', gap: '8px', marginBottom: '20px'}}>
  <img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" />
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" />
  <img src="https://img.shields.io/badge/Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" />
</div>

## 🏗️ 주요 기능 및 아키텍처

UserService는 크게 **외부 사용자 요청(인증)**, **내부 시스템 요청(Internal)**, **(비동기 이벤트 처리(Saga))** 세 가지 흐름으로 동작합니다.

### 1. 인증 및 회원 관리 (Authentication)
사용자의 회원가입 및 로그인을 처리합니다. 비밀번호는 **BCrypt** 알고리즘으로 암호화되어 저장되며, 로그인 성공시 **JWT 토큰** 생성하고 **Redis** 저장소에 RefreshToken을 저장한 뒤 AccessToken, RefreshToken을 반환합니다.
```mermaid
sequenceDiagram
    participant Client as 👤 Client
    participant Gateway as 🚪 Gateway
    participant User as 👤 User Service
    participant DB as 🗄️ DB

    Client->>Gateway: 로그인 요청 (POST /login)
    Gateway->>User: 요청 라우팅
    User->>DB: 사용자 조회 (By Email)
    DB-->>User: User Entity (Encrypted PW)
    User->>User: 패스워드 검증 (BCrypt)
    
    alt 검증 성공
        User-->>Client: 200 OK + JWT Token
    else 검증 실패
        User-->>Client: 401 Unauthorized
    end
```

### 2. Internal API
주문 서비스 등 다른 마이크로 서비스에서 사용자 정보를 동기적으로 조회해야할때 사용합니다.
- **보안**: `/internal/**` 경로는 Gateway의 InternalBlockFilter에 의해 외부 접근이 차단되어 외부에서 호출될 수 없습니다.
- **주요 API** : `/{userId}/order-info`: 주문시 회원 정보와 현재 포인트 잔액을 확인합니다.

### 3. Saga 패턴 & 포인트 처리
주문 시스템의 Saga 패턴에 참여하여, 주문 생성시 포인트를 차감하는 역할을 수행합니다.
```mermaid
%%{init: {'theme': 'base', 'themeVariables': { 'darkMode': false }}}%%
sequenceDiagram
    autonumber
    participant Kafka as 📨 Kafka
    participant User as 👤 User Service
    participant DB as 🗄️ DB (User Table)

    %% ---------------------------------------------------------
    %% 1. 정상 흐름 (포인트 차감)
    %% ---------------------------------------------------------
    rect rgb(240, 240, 255)
        Note over Kafka, User: ⚡ Case 1: 포인트 차감 요청 (Normal)
        
        Kafka->>User: 📥 메시지 수신 (Topic: user.saga.command)<br/>Payload: { Deduct Payload.. }
        
        User->>DB: 트랜잭션 시작 & 잔액 조회
        
        alt 잔액 충분
            User->>DB: 포인트 차감 (Update)
            User-->>Kafka: 📤 성공 이벤트 발행 (Topic: user.saga.reply)<br/>Status: SUCCESS
        else 잔액 부족
            User-->>Kafka: 📤 실패 이벤트 발행 (Topic: user.saga.reply)<br/>Status: FAIL
        end
    end

    %% ---------------------------------------------------------
    %% 2. 보상 트랜잭션 (롤백)
    %% ---------------------------------------------------------
    rect rgb(255, 240, 240)
        Note over Kafka, User: ↩️ Case 2: 보상 트랜잭션 (Compensation)
        Note right of Kafka: 이후 로직(재고/쿠폰) 실패 시<br/>Order 서비스가 롤백 명령 전송

        Kafka->>User: 📥 롤백 메시지 수신 (Topic: user.saga.command)<br/>Payload: { Refund Payload... }
        
        User->>DB: 포인트 재적립 (Refund)
        User-->>Kafka: 📤 롤백 완료 발행 (Topic: user.saga.reply)<br/>Status: SUCCESS
    end
```

## 💻 핵심 구현 코드

### 1. 로그인 토큰 생성
회원 검증을 수행하고 토큰 생성후 **Redis**에 리프레시 토큰을 저장한 뒤 **AccessToken**은 응답 바디, **RefreshToken**은 쿠키에 저장되어 응답이 반환됩니다.

```java
public TokenData login(String email, String password) {
    User user = findByEmailOrThrow(email);
    // 비밀번호 일치 확인
    validatePassword(password, user.getEncryptedPwd());
    // 토큰 (AccessToken, RefreshToken) 생성
    TokenData tokenData = tokenGenerator.generateTokenData(user.getId(), user.getRole());
    // Refresh Token 저장
    RefreshToken refreshToken = RefreshToken.create(user.getId(), tokenData.getRefreshToken());
    tokenRepository.save(refreshToken, tokenGenerator.getRefreshTokenExpiration());
    return tokenData;
}
```

### 2. Internal API
주문시 사용자 정보를 조회하기 위한 **내부 API** 입니다.
회원 기본정보(이름, 전화번호 등), 포인트 잔액을 반환합니다.
```java
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{userId}/order-info")
    public ResponseEntity<UserOrderResponse> getUserInfoForOrder(@PathVariable("userId") Long userId){
        UserOrderResponse response = userService.getUserInfoForOrder(userId);
        return ResponseEntity.ok(response);
    }
}
```

### 3. Saga 포인트 차감
주문 생성 이벤트 발행시 **Kafka 리스너**를 통해 비동기적으로 포인트 차감 요청을 처리하며, 처리 결과에 따라 성공,실패 이벤트를 다시 발행해 데이터 일관성을 유지합니다.
또한 롤백 이벤트시에도 비동기적으로 포인트 복구를 처리합니다.
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaProcessor {
    private final UserService userService;
    private final SagaEventProducer sagaEventProducer;

    public void userSagaProcess(UserSagaCommand command) {
        try {
            // 포인트 처리 성공 이벤트 발행
            processPointCommand(command.getType(), command.getUserId(), command.getUsedPoint());
            sagaEventProducer.sendSagaSuccess(command.getSagaId(), command.getOrderNo());
        } catch (BusinessException e) { // 포인트 처리 실패시 
            handleException(command, e.getErrorCode().name(), e.getMessage());
        } catch (Exception e) {
            handleException(command, "SYSTEM_ERROR", "시스템 오류");
        }
    }
    ...
}
```

## 🗄️ 데이터 모델 (Data Model)

User 서비스는 **Database per Service** 패턴을 따르며, 독립적인 데이터베이스(`users`)를 가집니다.

```mermaid
erDiagram
    USERS {
        bigint id PK "Auto Increment"
        varchar email UK "이메일 (로그인 ID)"
        varchar name "회원 이름"
        varchar encrypted_pwd "BCrypt 암호화 PW"
        varchar phone_number "휴대전화번호"
        varchar gender "성별 (MALE, FEMALE)"
        date birth_date "생년월일"
        bigint point "보유 포인트 (Default 0)"
        varchar role "권한 (ROLE_USER, ROLE_ADMIN)"
        datetime created_at "가입일시"
        datetime updated_at "수정일시"
    }
```
# 향후 개선 계획
- [] Refresh 토큰을 사용한 토큰 재발급
- [] 사용자 도메인 API 추가