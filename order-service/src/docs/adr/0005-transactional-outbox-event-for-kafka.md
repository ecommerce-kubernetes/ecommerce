# ADR-0005: SAGA 오케스트레이터 메시지 이중 쓰기 방지를 위한 트랜잭션 동기화 이벤트

- **Status:** Accepted
- **Date:** 2026-06-12
- **Author:** 최민식
- **Tags:** #SAGA, #Dual-Write #Spring-Event
-
## 1. Context (배경 및 문제 상황)

`OrderSagaManager`가 SAGA 처리 로직(history 저장, instance 스텝 변경)을 수행한 후 다음 SAGA를 진행하기 위해 마이크로 서비스로 Kafka 메시지를 발행하여야 한다
`OrderSagaManager`의 `handleReply` 에는 `@Transactional`이 적용되어 있고 이 메서드에서 kafka 메시지 발행 로직을 수행한다면 
만약 SAGA 로직 수행중 오류가 발행하여 데이터가 롤백되면 주문과 instance 상태는 롤백처리 되지만 이미 발행된 kafka 메시지는 취소할 수 없다
이렇게 유령 Kafka 메시지는 Saga 참여 서비스로 하여금 이미 롤백된 saga 에 대한 정방향 처리를 하게 되어 데이터 정합성을 깨뜨릴 수 있다.
## 2. Alternatives (고려했던 대안들)

1. **[대안 1: SagaManager 비지니스 로직에서 직접 카프카 호출]**
    - 설명: `Manager` 내에서 DB 업데이트와 Kafka 메시지 발행을 순차적으로 직접 실행
    - 장점: 구현이 매우 간단함.
    - 단점: 트랜잭션 롤백 시 메시지 발행을 취소할 수 없어 이중 쓰기 문제가 발행할 수 있다
2. **[대안 2: 물리적 Outbox 테이블 + 풀링 조회후 메시지 발행]**
    - 설명: 메시지를 바로 발행하는것이 아니라 Saga 로직(인스턴스 상태 전이) 내에서 `outbox`테이블에 페이로드 메시지를 INSERT 
           그 후 별도의 스케줄러가 `outbox` 테이블을 읽어 카프카 메시지를 발행
    - 장점: DB 커밋과 메시지 발송이 원자적으로 보장되며 카프카 장애시에도 메시지가 DB에 안전하게 보관됨
    - 단점: 인프라 구성 및 추가 테이블, 스케줄러 개발 비용이 필요함
3. **[대안 3: Spring Application Event 기반의 트랜잭션 동기화 이벤트 사용]**
    - 설명: `Manager`는 카프카를 호출하지 않고 Saga 상태 전이 이후 `ApplicationEvent`를 발행하고 이를 수신하는 카프카 발송 리스너가
           `@TransactionalEventListener(phase = TransactionalPhase.AFTER_COMMIT)` 옵션을 통해 DB 트랜잭션이 커밋 된 이후에만 
           카프카 메시지가 발송되도록 구성한다
   
## 3. Decision (결정)

- **[대안 3: Spring Application Event 기반의 트랜잭션 동기화 이벤트 사용]** 를 선택
- **이유:** SAGA 오케스트레이션에서 발생할 수 있는 트랜잭션 롤백 상황에서 메시지가 발송되는 문제를 효율적으로 차단 가능하며
            대안 2 방식 보다 구현이 간단하며 이중 쓰기 문제를 방어하는데 충분하다

## 4. Consequences (결과 및 영향)

### 👍 Positive (얻게 된 것)
- 안전성 확보: 로직 수행중 예외 발생시 DB가 롤백되면 이벤트 리스너가 아예 동작하지 않으므로 유령 Kafka 메시지가 발행될 문제가 없다
- 결합도 감소: `OrderSagaManager`는 도메인 상태만 조작하고 이벤트를 메모리에 던지는 비지니스 책임만 수행하며 Kafka 통신은 `SagaEventPublisher`로 분리된다

### 👎 Negative (잃은 것 / 감당할 것)
- 메모리 휘발성 리스크: DB 커밋은 성공했으나 `AFTER_COMMIT` 리스너가 카프카로 메시지를 발송하기 직전 서버가 다운된다면 메시지가 영구 유실될 수 있음
- 해당 문제는 스케줄러를 통해 정방향 고아 객체 감지 및 보상 처리를 통해 조치된다
