# [유저 애플리케이션 서비스] 도메인 모델 명세서

> 본 문서는 `유저 서비스`의 비지니스 요구 사항을 해결하기 위한 도메인 객체 구조, 역할, 핵심 비지니스 규칙을 정의한다.

## 1. 개요 (Overview)

- **도메인 목적**: 사용자 정보, 인증처리 등 사용자 관련 흐름을 관리한다.
- **주요 아키텍처 패턴**: `Domain-Driven Design`, `Hexagonal Architecture`, `SAGA pattern`

## 2. 애그리거트 명세 (Aggregate Specifications)

### 2.1 리프레시 토큰 (`RefreshToken`- Aggregate Root)

사용자의 리프레시 토큰 정보

### 2.1.1 속성 (Attribute)

| 필드명      | 타입         | 설명       |
|----------|------------|----------|
| `userId` | `Long`     | 사용자 식별자  |
| `token`  | `String`   | 리프레시 토큰  |
| `ttl`    | `Duration` | 토큰 만료 기간 |

### 2.1.2 핵심 도메인 규칙 (Invariants / Business Rules)

- [규칙 1: 리프레시 토큰을 생성할때는 필수값이 존재해야한다]
    1. 리프레시 토큰을 생성할때는 `userId`, `token`, `ttl` 값이 필수적으로 필요하다.

### 2.1.3 주요 행위 (Behavior / Commands)

| 메서드명/행위         | 파라미터                        | 반환값            | 비지니스 의도 및 제약               |
|-----------------|-----------------------------|----------------|----------------------------|
| `create`        | `CreateRefreshTokenContext` | `RefreshToken` | 리프레시 토큰을 생성한다.             |
| `validateToken` | `String`                    | `void`         | 리프레시 토큰과 현재 토큰이 동일한지 검증한다. |
