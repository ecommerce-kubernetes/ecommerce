# [상품 애플리케이션 서비스] 도메인 모델 명세서

> 본 문서는 `상품 서비스`의 비지니스 요구 사항을 해결하기 위한 도메인 객체 구조, 역할, 핵심 비지니스 규칙을 정의한다. (현재는 카테고리, 옵션, 상품 도메인이 작성되어 있다.)

## 1. 개요 (Overview)

- **도메인 목적**: 상품을 분류하는 계층형(트리) 카테고리 구조를 관리하고, 카테고리 생성·수정·이동·삭제 시 depth·path 정합성, 형제 카테고리 간 이름 중복 방지, 상품이 등록된 카테고리 보호 규칙을
  보장한다.
- **주요 아키텍처 패턴**: `Domain-Driven Design`, `Hexagonal Architecture`

- **상품 도메인 목적**: 상품(Product)의 생애주기(준비중·판매중·판매중지·삭제)와 옵션 축(옵션 타입 구성)·판매 단위(변형)의 구조적 정합성을 관리한다. 옵션 축이 바뀌면 기존 변형이 무효화된다는 것을
  스스로 보장하고, 삭제는 되돌릴 수 없는 소프트 삭제로 처리하며 보유 변형에 캐스케이드하고, Category/Option 등 다른 애그리거트는 엔티티 참조가 아니라 id(categoryId,
  optionTypeId, optionValueId) + 포트로만 참조해 애그리거트 경계를 지킨다. order-service 등 외부 서비스가 참조하는 productId·variantId는 물리 삭제 없이
  영속적으로 유지된다.

## 2. 애그리거트 명세 (Aggregate Specifications)

### 2.1 카테고리 (`Category` - Aggregate Root)

상품을 분류하는 계층형 카테고리 정보를 관리하며, 부모-자식 관계에 따른 depth·path 정합성과 최대 깊이 제한을 스스로 보장한다.

### 2.1.1 속성 (Attribute)

| 필드명         | 타입               | 설명                                      |
|-------------|------------------|-----------------------------------------|
| `id`        | `Long`           | 카테고리 식별자                                |
| `name`      | `String`         | 카테고리 이름                                 |
| `depth`     | `Integer`        | 카테고리 깊이(최상위 = 1)                        |
| `path`      | `String`         | 루트부터 자신까지의 식별자 경로(`/`로 구분, 예: `1/5/12`) |
| `imagePath` | `String`         | 카테고리 이미지 경로                             |
| `parent`    | `Category`       | 부모 카테고리(최상위 카테고리는 `null`)               |
| `children`  | `List<Category>` | 자식 카테고리 목록                              |

### 2.1.2 핵심 도메인 규칙 (Invariants / Business Rules)

- [규칙 1: 카테고리 생성 시 식별자, 이름, 경로는 필수이다.]
    1. `id`, `name`, `path` 중 하나라도 없으면 카테고리를 생성할 수 없다.
- [규칙 2: 최상위 카테고리는 depth 1, path는 자신의 id로 설정된다.]
    1. `createRoot`로 생성된 카테고리는 부모가 없으며, depth는 1, path는 자신의 `id` 문자열이다.
- [규칙 3: 하위 카테고리 생성 시 부모는 필수이다.]
    1. `createChild` 호출 시 `parent`가 `null`이면 카테고리를 생성할 수 없다.
- [규칙 4: 카테고리 깊이는 최대 5단계를 초과할 수 없다.]
    1. 부모 카테고리의 depth가 5(`MAX_DEPTH`)이면 그 하위에 자식 카테고리를 생성하거나 다른 카테고리를 그 아래로 이동시킬 수 없다.
- [규칙 5: 하위 카테고리의 depth와 path는 부모를 기준으로 파생된다.]
    1. 자식 카테고리의 depth는 부모의 depth + 1이며, path는 부모의 path에 자신의 id를 이어붙인 값이다.
- [규칙 6: 카테고리를 자기 자신 또는 자신의 하위 카테고리로 이동할 수 없다.]
    1. 이동하려는 새 부모가 카테고리 자기 자신이면 예외가 발생한다.
    2. 이동하려는 새 부모의 path가 자신의 path로 시작하면(자신의 하위 카테고리이면) 예외가 발생한다.
- [규칙 7: 카테고리 이동 시 depth와 path가 새 부모를 기준으로 재계산된다.]
    1. 새 부모가 있으면 depth는 새 부모의 depth + 1, path는 새 부모의 path에 자신의 id를 이어붙인 값으로 갱신된다.
    2. 새 부모가 없으면(최상위로 이동) depth는 1, path는 자신의 id 문자열로 갱신된다.
- [규칙 8: 자식이 없는 카테고리는 리프(leaf) 카테고리이다.]
    1. `children`이 비어 있으면 `isLeaf()`는 `true`를 반환한다.
- [규칙 9: 조상 카테고리가 이동하면 후손 카테고리의 경로도 함께 갱신되어야 한다.]
    1. `relocatePrefix` 호출 시 대상 카테고리의 path가 전달받은 기존 경로 접두사로 시작하지 않으면 예외가 발생한다.
    2. path의 접두사를 새 접두사로 치환하고, depth에 전달받은 변화량(`depthDelta`)만큼 더한다.

### 2.1.3 주요 행위 (Behavior / Commands)

| 메서드명/행위          | 파라미터                                           | 반환값        | 비지니스 의도 및 제약                                                          |
|------------------|------------------------------------------------|------------|-----------------------------------------------------------------------|
| `createRoot`     | `id`, `name`, `imagePath`                      | `Category` | 최상위 카테고리를 생성한다. depth는 1, path는 자신의 id로 설정된다.                         |
| `createChild`    | `id`, `name`, `imagePath`, `parent`            | `Category` | 하위 카테고리를 생성한다. 부모가 없거나 부모의 depth가 최대이면 예외가 발생한다.                      |
| `update`         | `name`, `imagePath`                            | `void`     | 카테고리의 이름과 이미지 경로를 수정한다.                                               |
| `isRoot`         | 없음                                             | `boolean`  | 부모가 없는(최상위) 카테고리인지 확인한다.                                              |
| `isLeaf`         | 없음                                             | `boolean`  | 자식이 없는 카테고리인지 확인한다.                                                   |
| `moveParent`     | `newParent`                                    | `void`     | 카테고리의 부모를 변경한다. 자기 자신/하위 카테고리로는 이동할 수 없고, 새 부모의 depth가 최대이면 이동할 수 없다. |
| `relocatePrefix` | `oldPathPrefix`, `newPathPrefix`, `depthDelta` | `void`     | 조상 카테고리 이동에 따라 후손 카테고리의 path 접두사와 depth를 함께 갱신한다.                     |

---

### 2.2 옵션 타입 (`OptionType` - Aggregate Root)

상품에 부여할 수 있는 옵션의 종류(예: 색상, 사이즈)와 그에 속한 옵션 값 목록을 관리하며, 옵션 값 이름이 같은 옵션 타입 내에서 중복되지 않도록 스스로 보장한다.

### 2.2.1 속성 (Attribute)

| 필드명            | 타입                  | 설명                |
|----------------|---------------------|-------------------|
| `id`           | `Long`              | 옵션 타입 식별자         |
| `name`         | `String`            | 옵션 타입 이름          |
| `optionValues` | `List<OptionValue>` | 옵션 타입에 속한 옵션 값 목록 |

### 2.2.2 핵심 도메인 규칙 (Invariants / Business Rules)

- [규칙 1: 옵션 타입 생성 시 식별자와 이름은 필수이다.]
    1. `id`, `name` 중 하나라도 없으면 옵션 타입을 생성할 수 없다.
- [규칙 2: 옵션 타입 생성 시 옵션 값 이름은 서로 중복될 수 없다.]
    1. `create` 호출 시 전달받은 옵션 값 컨텍스트 목록에 동일한 이름이 둘 이상 있으면 옵션 타입을 생성할 수 없다.
- [규칙 3: 같은 옵션 타입 내에서 옵션 값 이름은 항상 유일해야 한다.]
    1. `addOptionValue` 호출 시 기존 옵션 값과 이름이 같으면 추가할 수 없다.
    2. `updateOptionValue` 호출 시 자기 자신을 제외한 다른 옵션 값과 이름이 같으면 수정할 수 없다(자기 자신과 동일한 이름으로의 수정은 허용된다).
- [규칙 4: 옵션 값을 수정하거나 삭제하려면 해당 옵션 타입에 속한 옵션 값이어야 한다.]
    1. 존재하지 않는(또는 다른 옵션 타입에 속한) `optionValueId`로 `updateOptionValue`, `deleteOptionValue`를 호출하면 예외가 발생한다.

### 2.2.3 주요 행위 (Behavior / Commands)

| 메서드명/행위             | 파라미터                       | 반환값           | 비지니스 의도 및 제약                                                       |
|---------------------|----------------------------|---------------|--------------------------------------------------------------------|
| `create`            | `CreateOptionTypeContext`  | `OptionType`  | 옵션 타입과 그에 속한 옵션 값들을 함께 생성한다. 옵션 값 이름이 중복되면 예외가 발생한다.               |
| `update`            | `name`                     | `void`        | 옵션 타입의 이름을 수정한다.                                                   |
| `addOptionValue`    | `CreateOptionValueContext` | `OptionValue` | 옵션 값을 추가한다. 동일한 이름의 옵션 값이 이미 존재하면 예외가 발생한다.                        |
| `updateOptionValue` | `optionValueId`, `newName` | `void`        | 옵션 값의 이름을 수정한다. 대상 옵션 값이 없으면 예외가 발생하고, 다른 옵션 값과 이름이 중복되면 예외가 발생한다. |
| `deleteOptionValue` | `optionValueId`            | `void`        | 옵션 값을 삭제한다. 대상 옵션 값이 없으면 예외가 발생한다.                                 |

### 2.2.4 내부 구성 요소(Entities & Value Objects)

#### 하위 엔티티: 옵션 값 (`OptionValue`)

**역할**: 옵션 타입에 종속된 개별 옵션 값(예: 색상 옵션의 `BLUE`, `RED`)을 관리한다.

**속성 (Attribute)**

| 필드명          | 타입           | 설명       |
|--------------|--------------|----------|
| `id`         | `Long`       | 옵션 값 식별자 |
| `optionType` | `OptionType` | 소속 옵션 타입 |
| `name`       | `String`     | 옵션 값 이름  |

**도메인 규칙**

- [규칙 1: 옵션 값 생성 시 식별자, 소속 옵션 타입, 이름은 필수이다.]
    1. `id`, `optionType`, `name` 중 하나라도 없으면 옵션 값을 생성할 수 없다.

**주요 행위**

| 메서드명/행위            | 파라미터                                     | 반환값           | 비지니스 의도 및 제약                                           |
|--------------------|------------------------------------------|---------------|--------------------------------------------------------|
| `create`           | `CreateOptionValueContext`, `OptionType` | `OptionValue` | 옵션 값을 생성한다. 소속 옵션 타입을 설정한다.                            |
| `update`           | `newName`                                | `void`        | 옵션 값의 이름을 수정한다. (`OptionType`에서만 호출되는 패키지 전용 메서드)      |
| `detachOptionType` | 없음                                       | `void`        | 소속 옵션 타입과의 연관관계를 해제한다. 옵션 값 삭제 시 `OptionType`에서만 호출된다. |

---

### 2.3 상품 (`Product` - Aggregate Root)

상품의 기본 정보(이름·설명·카테고리), 옵션 축(옵션 타입 구성), 판매 단위(변형) 목록을 관리하는 애그리거트 루트. `ProductVariant`/`ProductOptionType`/
`ProductMainImage`/`ProductDetailImage`는 같은 애그리거트에 속한 자식 엔티티로 cascade=ALL로 함께 관리되지만, `Category`/`OptionType`/
`OptionValue`는 다른 애그리거트이므로 엔티티가 아니라 id(`categoryId`, `optionTypeId`, `optionValueId`) + 포트(`ProductCategoryPort`,
`ProductOptionPort`)로만 참조한다.

### 2.3.1 속성 (Attribute)

| 필드명                   | 타입                         | 설명                                                      |
|-----------------------|----------------------------|---------------------------------------------------------|
| `id`                  | `Long`                     | 상품 식별자                                                  |
| `name`                | `String`                   | 상품 이름                                                   |
| `categoryId`          | `Long`                     | 소속 카테고리 식별자(`Category` 애그리거트 참조, 엔티티 대신 id로 참조)         |
| `status`              | `ProductStatus`            | 상품 상태(`PREPARING`/`ON_SALE`/`STOP_SALE`/`DELETED`)      |
| `description`         | `String`                   | 상품 설명(선택값)                                              |
| `publishedAt`         | `LocalDateTime`            | 최초 게시 시각                                                |
| `saleStoppedAt`       | `LocalDateTime`            | 판매 중지 시각                                                |
| `deletedAt`           | `LocalDateTime`            | 삭제 시각                                                   |
| `thumbnail`           | `String`                   | 대표 썸네일 이미지 경로(메인 이미지 첫 번째 항목에서 파생)                      |
| `rating`              | `Double`                   | 평점(review-service가 내려주는 값을 캐싱, 생성 시 `0.0`)              |
| `reviewCount`         | `Long`                     | 리뷰 개수(review-service가 내려주는 값을 캐싱, 생성 시 `0L`)            |
| `popularityScore`     | `Double`                   | 인기 점수(product-service가 배치로 합성, 생성 시 `0.0`, 고객 응답에는 비노출) |
| `representativePrice` | `SalePrice`                | 대표(최저가) 변형의 판매 가격(임베디드 값 객체)                            |
| `variants`            | `List<ProductVariant>`     | 보유한 상품 변형(판매 단위) 목록                                     |
| `productOptionTypes`  | `List<ProductOptionType>`  | 상품에 설정된 옵션 축(옵션 타입) 목록                                  |
| `mainImages`          | `List<ProductMainImage>`   | 메인 이미지 목록                                               |
| `detailImages`        | `List<ProductDetailImage>` | 설명 이미지 목록                                               |

### 2.3.2 핵심 도메인 규칙 (Invariants / Business Rules)

- [규칙 1: 상품 생성 시 식별자, 이름, 카테고리, 상태는 필수이다.]
    1. `id`, `name`, `categoryId`, `status` 중 하나라도 없으면 상품을 생성할 수 없다. `description`은 선택값이다.
    2. 생성된 상품의 상태는 항상 `PREPARING`으로 고정되고, `rating`/`reviewCount`/`popularityScore`는 각각 `0.0`/`0L`/`0.0`으로 초기화된다.
- [규칙 2: 삭제된(`DELETED`) 상품은 이름·설명·카테고리를 수정할 수 없다.]
    1. `status`가 `DELETED`인 상품에 `update`를 호출하면 예외가 발생한다.
- [규칙 3: 옵션 타입은 최대 3개까지, 서로 중복 없이 설정할 수 있다.]
    1. `registerOptionTypes` 호출 시 전달받은 옵션 타입 개수가 3개(`MAX_OPTION_SIZE`)를 초과하면 예외가 발생한다.
    2. 전달받은 옵션 타입 id 목록에 중복이 있으면 예외가 발생한다.
- [규칙 4: 옵션 축(옵션 타입 구성)은 상품이 `ON_SALE`·`DELETED`가 아닐 때만 재설정할 수 있고, 재설정 시 보유한 모든 상품 변형이 함께 단종 처리된다.]
    1. `status`가 `ON_SALE` 또는 `DELETED`이면 옵션 축을 재설정할 수 없다.
    2. 그 외 상태(`PREPARING`, `STOP_SALE`)에서는 재설정이 허용되며, 옵션 타입 목록을 교체하기 직전 보유한 모든 `ProductVariant`에 단종 처리(`discontinue`)가
       전파된다.
    3. 옵션 타입 목록은 항상 기존 목록을 비우고 새로 구성된다, 부분 추가·삭제·순서 변경을 할 수 없다
    4. `ON_SALE` 상품이 옵션 축을 바꾸려면 먼저 `close()`로 `STOP_SALE`로 전환해야 한다(규칙 8 참고).
- [규칙 5: 상품 변형은 삭제된(`DELETED`) 상품에만 추가할 수 없다 — `ON_SALE` 상태에도 추가할 수 있으며, 기존 변형은 건드리지 않는 추가(append)형이다.]
    1. `status`가 `DELETED`이면 변형을 추가할 수 없다. `PREPARING`/`ON_SALE`/`STOP_SALE`에서는 모두 허용된다.
    2. 추가하려는 각 변형의 옵션 값은 개수와 소속 옵션 타입이 상품에 설정된 옵션 타입 전체와 정확히 일치해야 한다(타입마다 정확히 값 하나, 타입 누락·중복 모두 금지).
    3. 이미 존재하는 변형과 동일한 옵션 값 조합으로는 새 변형을 추가할 수 없되, 단종(`DISCONTINUED`)된 변형은 이 중복 판단에서 제외한다(단종된 변형은 없는 것으로 취급).
    4. 새로 추가된 변형은 항상 `PREPARING` 상태로 시작한다 — 상품이 이미 `ON_SALE`이어도 개별 변형은 `publish()`를 통해서만 `ACTIVE`가 된다(규칙 8 참고).
- [규칙 6: 상품 삭제는 소프트 삭제이며, `ON_SALE` 상태에서는 삭제할 수 없고, 이미 삭제된 상품은 다시 삭제할 수 없다.]
    1. `status`가 `ON_SALE`이면 `deleted()`를 호출할 수 없다(먼저 `close()` 필요).
    2. 이미 `status`가 `DELETED`인 상품에 `deleted()`를 다시 호출하면 예외가 발생한다.
    3. 삭제 시 `status`는 `DELETED`로 바뀌고 `deletedAt`이 기록되며, 보유한 모든 `ProductVariant`에 단종 처리가 전파된다.
    4. 물리 삭제(리포지토리 `delete`)는 어떤 경우에도 수행하지 않는다 — order-service 등 외부 서비스가 참조하는 `productId`/`variantId`가 영구히 유효해야 하기 때문이다.
       `PREPARING`처럼 판매 이력이 없는 상품도 예외 없이 동일하게 소프트 삭제한다.
- [규칙 7: 메인 이미지·설명 이미지는 삭제된 상품에는 추가할 수 없고, 추가할 때마다 전체가 교체된다.]
    1. `status`가 `DELETED`이면 이미지를 추가할 수 없다.
    2. 메인 이미지 추가 시 기존 목록은 비워지고 새 목록으로 교체되며, 첫 번째 이미지가 `thumbnail`로 지정된다.
    3. 설명 이미지도 동일하게 풀 리플레이스로 교체된다.
- [규칙 8 — 상품은 게시 전제조건을 통과해야 `ON_SALE`로 전환되고, 판매 중지는 언제든 가역적으로 전환할 수 있으며, 두 전이는 상품 변형의 단종 여부와 독립적이다.]
    1. `close()`는 `ON_SALE` 상태의 상품만 `STOP_SALE`로 전환할 수 있고, `publish()`로 다시 `ON_SALE`로 되돌릴 수 있다.
    2. `publish()`는 보유 변형 1개 이상, 썸네일 존재, 설명 이미지 1개 이상, 판매가/정가 유효성(판매가 ≤ 정가), 할인율 유효성을 모두 만족해야 호출할 수 있다.
    3. `publish()` 시 보유한 `PREPARING` 상태의 변형은 `ACTIVE`로 전이된다.
    4. `close()`/`publish()`는 `ProductVariant.discontinue()`와 서로 자동으로 연동되지 않는다 — 판매 중지·재게시는 되돌릴 수 있는 전이이고 단종은 되돌릴 수 없는
       전이이므로 완전히 독립적으로 취급한다.
- [규칙 9: `rating`/`reviewCount`/`popularityScore`는 상품 스스로 계산하지 않는다.]
    1. 생성 시 `0.0`/`0L`/`0.0`으로 고정 초기화된다.
    2. `rating`/`reviewCount`는 review-service가 비동기 이벤트로 내려주는 절대값을 캐싱하는 용도이고, `popularityScore`는 product-service가 여러 서비스
       신호(판매량·리뷰 등)를 합성해 배치로 계산하는 값이다 — 둘 다 `Product` 도메인 내부 로직으로 계산되지 않는다.

### 2.3.3 주요 행위 (Behavior / Commands)

| 메서드명/행위                    | 파라미터                                              | 반환값       | 비지니스 의도 및 제약                                                                                                      |
|----------------------------|---------------------------------------------------|-----------|-------------------------------------------------------------------------------------------------------------------|
| `create`                   | `CreateProductContext`                            | `Product` | 상품을 생성한다. 상태는 `PREPARING`으로 고정, `rating`/`reviewCount`/`popularityScore`는 `0`으로 초기화된다.                            |
| `update`                   | `UpdateProductContext`                            | `void`    | 이름·설명·카테고리를 수정한다. 삭제된 상품이면 예외가 발생한다.                                                                              |
| `registerOptionTypes`      | `List<RegisterOptionTypeContext>`, `registeredAt` | `void`    | 옵션 타입 구성을 풀 리플레이스로 재설정한다. `ON_SALE`/`DELETED` 상태이거나 3개를 초과하거나 중복 id가 있으면 예외가 발생하고, 그 외에는 보유 변형을 모두 단종 처리한 뒤 교체한다. |
| `addMainImages`            | `List<AddMainImageContext>`                       | `void`    | 메인 이미지 목록을 풀 리플레이스로 교체하고 첫 이미지를 썸네일로 지정한다. 삭제된 상품이면 예외가 발생한다.                                                     |
| `addDetailImages`          | `List<AddDetailImageContext>`                     | `void`    | 설명 이미지 목록을 풀 리플레이스로 교체한다. 삭제된 상품이면 예외가 발생한다.                                                                      |
| `addVariants`              | `List<AddVariantContext>`                         | `void`    | 새 상품 변형을 추가한다(기존 변형은 건드리지 않음). 삭제된 상품이거나 옵션 값 구성이 상품 옵션 타입과 맞지 않거나 비단종 변형과 조합이 중복되면 예외가 발생한다.                     |
| `deleted`                  | `deletedAt`                                       | `void`    | 상품을 소프트 삭제한다. `ON_SALE` 상태이면, 또는 이미 삭제된 상태이면 예외가 발생한다. 보유한 모든 변형에 단종 처리를 전파한다.                                    |
| `close` *(설계 확정, 구현 예정)*   | 없음                                                | `void`    | `ON_SALE` 상품을 `STOP_SALE`로 전환한다.                                                                                  |
| `publish` *(설계 확정, 구현 예정)* | 없음                                                | `void`    | 게시 전제조건을 검증한 뒤 상품을 `ON_SALE`로 전환하고, 보유한 `PREPARING` 변형을 `ACTIVE`로 전이시킨다.                                          |

### 2.3.4 내부 구성 요소(Entities & Value Objects)

#### 하위 엔티티: 상품 옵션 타입 (`ProductOptionType`)

**역할**: 상품에 설정된 옵션 축(옵션 타입) 하나와 그 표시 순서를 관리한다. `Product` 생명주기에 완전히 종속되며 별도로 소프트 삭제되지 않는다.

**속성 (Attribute)**

| 필드명            | 타입        | 설명                                                   |
|----------------|-----------|------------------------------------------------------|
| `id`           | `Long`    | 상품 옵션 타입 식별자                                         |
| `product`      | `Product` | 소속 상품(같은 애그리거트, 엔티티 참조)                              |
| `optionTypeId` | `Long`    | 참조하는 옵션 타입 식별자(`OptionType` 애그리거트 참조, 엔티티 대신 id로 참조) |
| `displayOrder` | `Integer` | SKU 내 옵션 표시 순서(옵션 타입 목록 내 위치)                        |

**도메인 규칙**

- [규칙 1: 생성 시 식별자, 소속 상품, 옵션 타입 식별자, 표시 순서는 모두 필수이다.]
    1. `id`, `product`, `optionTypeId`, `displayOrder` 중 하나라도 없으면 생성할 수 없다.

**주요 행위**

| 메서드명/행위  | 파라미터                                                   | 반환값                 | 비지니스 의도 및 제약    |
|----------|--------------------------------------------------------|---------------------|-----------------|
| `create` | `RegisterOptionTypeContext`, `Product`, `displayOrder` | `ProductOptionType` | 상품 옵션 타입을 생성한다. |

#### 하위 엔티티: 상품 변형 (`ProductVariant`)

**역할**: 상품의 판매 단위(SKU)를 관리한다. 상품에 설정된 옵션 타입마다 옵션 값을 하나씩 가지며, 판매 가격·재고·상태(`PREPARING`·`ACTIVE`·`DISCONTINUED`)를 스스로 관리한다.

**속성 (Attribute)**

| 필드명                          | 타입                                | 설명                                            |
|------------------------------|-----------------------------------|-----------------------------------------------|
| `id`                         | `Long`                            | 상품 변형 식별자                                     |
| `product`                    | `Product`                         | 소속 상품(같은 애그리거트, 엔티티 참조)                       |
| `status`                     | `ProductVariantStatus`            | 상품 변형 상태(`PREPARING`/`ACTIVE`/`DISCONTINUED`) |
| `sku`                        | `String`                          | SKU 코드                                        |
| `salePrice`                  | `SalePrice`                       | 판매 가격(임베디드 값 객체)                              |
| `stock`                      | `Integer`                         | 재고 수량                                         |
| `discontinuedAt`             | `LocalDateTime`                   | 단종 시각(단종되지 않았으면 `null`)                       |
| `productVariantOptionValues` | `List<ProductVariantOptionValue>` | 이 변형이 가진 옵션 값 목록                              |

**도메인 규칙**

- [규칙 1: 생성 시 식별자, 소속 상품, 상태, SKU, 판매 가격은 필수이며, 재고는 0 이상이어야 한다.]
    1. `id`, `product`, `status`, `sku`, `salePrice` 중 하나라도 없으면 생성할 수 없다.
    2. `stock`이 음수이면 생성할 수 없다.
- [규칙 2: 생성된 상품 변형의 상태는 항상 `PREPARING`이다.]
    1. `create` 호출 시 상태는 무조건 `PREPARING`으로 설정된다(생성 즉시 `ACTIVE`가 될 수 없다).
- [규칙 3: `discontinue`는 단방향(비가역) 전이이다 — 재활성화 메서드는 존재하지 않는다.]
    1. `discontinued()` 호출 시 상태는 `DISCONTINUED`로, `discontinuedAt`은 전달받은 시각으로 고정되며, 이를 되돌리는 메서드는 없다.
    2. 이 메서드는 오직 소속 `Product`의 `deleted()` 캐스케이드, 또는 옵션 축 재설정 흐름(`registerOptionTypes`)에서만 호출된다.
- [규칙 4 — 설계 확정, 구현 예정: `PREPARING → ACTIVE` 전이는 소속 `Product`의 `publish()`에 의해서만 일어난다.]
    1. 이 전이와 `discontinue`(단종)는 서로 다른 트리거로 완전히 분리되어 있어, 판매 중지·재게시가 단종 상태에 영향을 주지 않는다.

**주요 행위**

| 메서드명/행위        | 파라미터                           | 반환값              | 비지니스 의도 및 제약                                               |
|----------------|--------------------------------|------------------|------------------------------------------------------------|
| `create`       | `AddVariantContext`, `Product` | `ProductVariant` | 상품 변형을 생성한다. 재고가 음수이면 예외가 발생한다. 상태는 항상 `PREPARING`으로 시작한다. |
| `discontinued` | `discontinuedAt`               | `void`           | 상품 변형을 단종 처리한다(비가역).                                       |

#### 하위 엔티티: 상품 변형 옵션 값 (`ProductVariantOptionValue`)

**역할**: 하나의 상품 변형이 특정 옵션 값을 가진다는 사실을 표현한다. `Option` 애그리거트의 `OptionValue`를 엔티티가 아니라 id로 참조한다.

**속성 (Attribute)**

| 필드명              | 타입               | 설명                                                   |
|------------------|------------------|------------------------------------------------------|
| `id`             | `Long`           | 상품 변형 옵션 값 식별자                                       |
| `productVariant` | `ProductVariant` | 소속 상품 변형(같은 애그리거트, 엔티티 참조)                           |
| `optionValueId`  | `Long`           | 참조하는 옵션 값 식별자(`OptionValue` 애그리거트 참조, 엔티티 대신 id로 참조) |

**도메인 규칙**

- [규칙 1: 생성 시 식별자, 소속 상품 변형, 옵션 값 식별자는 모두 필수이다.]
    1. `id`, `productVariant`, `optionValueId` 중 하나라도 없으면 생성할 수 없다.

**주요 행위**

| 메서드명/행위  | 파라미터                                    | 반환값                         | 비지니스 의도 및 제약      |
|----------|-----------------------------------------|-----------------------------|-------------------|
| `create` | `id`, `optionValueId`, `productVariant` | `ProductVariantOptionValue` | 상품 변형 옵션 값을 생성한다. |

#### 값 객체: 판매 가격 (`SalePrice`)

**역할**: 정가·할인율·할인금액·판매가를 하나로 묶어 관리하는 임베디드 값 객체. `Product`의 대표 가격(`representativePrice`)과 `ProductVariant`의 `salePrice`
양쪽에서 재사용된다.

**속성 (Attribute)**

| 필드명              | 타입        | 설명     |
|------------------|-----------|--------|
| `originalPrice`  | `Money`   | 정가     |
| `discountRate`   | `Integer` | 할인율(%) |
| `discountAmount` | `Money`   | 할인 금액  |
| `price`          | `Money`   | 실제 판매가 |

**도메인 규칙**

- [규칙 1: 생성 시 네 필드 모두 필수이다.]
    1. `originalPrice`, `discountRate`, `discountAmount`, `price` 중 하나라도 없으면 생성할 수 없다.

**주요 행위**

| 메서드명/행위 | 파라미터                                                       | 반환값         | 비지니스 의도 및 제약      |
|---------|------------------------------------------------------------|-------------|-------------------|
| `of`    | `originalPrice`, `discountRate`, `discountAmount`, `price` | `SalePrice` | 판매 가격 값 객체를 생성한다. |

#### 열거형: 상품 상태 (`ProductStatus`)

| 값           | 의미                                                                                |
|-------------|-----------------------------------------------------------------------------------|
| `PREPARING` | 판매 대기 상품(한 번도 게시되지 않았거나 옵션·변형을 준비 중인 상태)                                          |
| `ON_SALE`   | 판매중인 상품                                                                           |
| `STOP_SALE` | 판매 중지 상품(과거 판매 이력이 있고 일시적으로 중단된 상태, `close()`/`publish()`로 왕복 가능 — 전이 메서드는 구현 예정) |
| `DELETED`   | 삭제된 상품(소프트 삭제, 되돌릴 수 없음)                                                          |

#### 열거형: 상품 변형 상태 (`ProductVariantStatus`)

| 값              | 의미                                                |
|----------------|---------------------------------------------------|
| `PREPARING`    | 변형은 등록됐지만 아직 게시 전이라 판매 불가능한 상태                    |
| `ACTIVE`       | 게시되어 실제 판매 가능한 상태(`PREPARING → ACTIVE` 전이는 구현 예정) |
| `DISCONTINUED` | 영구 단종된 상태(`discontinue()`에 의해서만 도달, 비가역)          |
