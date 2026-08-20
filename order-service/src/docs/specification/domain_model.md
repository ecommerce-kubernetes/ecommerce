# [주문 애플리케이션 서비스] 도메인 모델 명세서

> 본 문서는 `주문 서비스`의 비지니스 요구 사항을 해결하기 위한 도메인 객체 구조, 역할, 핵심 비지니스 규칙을 정의한다.

## 1. 개요 (Overview)

- **도메인 목적**: 사용자의 장바구니, 주문서, 최종 주문 생성 및 결제 후 SAGA 까지의 전체 주문 흐름을 관리한다.
- **주요 아키텍처 패턴**: `Domain-Driven Design`, `Hexagonal Architecture`, `SAGA pattern`

## 2. 애그리거트 명세 (Aggregate Specifications)

### 2.1 `Cart` (Aggregate Root)

사용자의 주문 대기중인 상품 목록을 관리

### 2.1.1 속성 (Attribute)

| 필드명       | 타입             | 설명                    |
|-----------|----------------|-----------------------|
| id        | Long           | 장바구니 식별자              |
| userId    | Long           | 장바구니의 소유자 식별자         |
| cartItems | List<CartItem> | 장바구니에 담긴 개별 상품 항목 리스트 |

### 2.1.2 핵심 도메인 규칙 (Invariants / Business Rules)

- 규칙 1: 사용자 1명당 1개의 장바구니를 갖는다.
    1. 장바구니 생성시 `userId`는 반드시 존재해야 한다.
- 규칙 2: 장바구니에 상품을 추가할 때 최소 1개 이상의 항목이 전달 되어야 한다.
    1. `addItems(장바구니 항목 추가)` 호출 시 추가할 상품 목록이 비어있으면 장바구니 항목을 추가할 수 없다.
- 규칙 3: 장바구니 항목 수는 최대 20개를 초과할 수 없다.
    1. 새로운 상품을 추가할 때 현재 장바구니 항목 수가 20개 이상이면 추가할 수 없다.
- 규칙 4: 동일한 옵션의 상품은 장바구니에 중복된 항목으로 추가되지 않는다.
    1. 동일한 `productVariantId(상품 판매 단위 식별자)`의 상품이 이미 존재하면 새로운 항목을 생성하지 않고 기존 항목의 수량을 증가시킨다.
- 규칙 5: 장바구니 항목을 수정하거나 삭제할 때 해당 장바구니에 존재하는 항목만 대상이 될 수 있다.
    1. 존재하지 않는 `cartItemId(장바구니 항목 아이디)`로 수량을 변경하거나 삭제할 수 없다.

### 2.1.3 주요 행위 (Behavior / Commands)

장바구니 객체의 행위

| 메서드명/행위              | 파라미터                                 | 반환값              | 비지니스 의도 및 제약                                                                                                                          |
|----------------------|--------------------------------------|------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| `create`             | `userId`, `IdGenerator`              | `Cart`           | 특정 사용자의 장바구니를 생성한다.                                                                                                                   |
| `addItems`           | `AddCartItemsContext`, `IdGenerator` | `List<CartItem>` | 여러 상품을 장바구니에 추가한다. 추가할 항목이 비어있으면 안되며, 각 항목은 장바구니의 최대 항목수 제한을 준수해야한다. 동일한 `productVariantId` 가 이미 존재하면 새 항목을 생성하지 않고 기존 항목의 수량을 증가시킨다. |
| `updateItemQuantity` | `UpdateCartItemContext`              | `void`           | 장바구니에 존재하는 항목의 수량을 변경한다.                                                                                                              |
| `deleteItem`         | `cartItemId`                         | `void`           | 장바구니에 존재하는 항목을 제거한다.                                                                                                                  |

### 2.1.4 내부 구성 요소(Entities & Value Objects)

#### 하위 엔티티: 장바구니 항목 (`CartItem`)

**역할**: 장바구니에 담긴 개별 상품 정보를 관리.

**속성 (Attribute)**

| 필드명              | 타입      | 설명          |
|------------------|---------|-------------|
| id               | Long    | 장바구니 항목 아이디 |
| cart             | Cart    | 장바구니        |
| productVariantId | Long    | 상품 변형 아이디   |
| quantity         | Integer | 담은 수량       |

**도메인 규칙**

- 규칙 1: 상품 수량은 최소 1개 이상이어야 한다.
    1. `CartItem` 생성 및 수량 변경 시 수량이 0 이하면 생성 또는 변경할 수 없다.
- 규칙 2: 상품 수량은 상품별 최대 구매 수량을 초과할 수 없다.
    1. `CartItem` 생성, 기존 수량 증가, 수량 변경 시 지정된 `maxLimit`를 초과할 수 없다.
- 규칙 3: 수량 증가 후에도 최대 구매 수량 제한을 초과할 수 없다.
    1. 기존 수량에 추가 수량을 더한 값이 `maxLimit`를 초과하면 수량을 증가시킬 수 없다.

**주요 행위**

| 메서드명/행위          | 파라미터                                      | 반환값        | 비지니스 의도 및 제약                                                                  |
|------------------|-------------------------------------------|------------|-------------------------------------------------------------------------------|
| `create`         | `AddCartItemsContext.Item`, `IdGenerator` | `CartItem` | 상품 변형과 수량을 기반으로 장바구니 항목을 생성한다. 식별자를 생성하고, 수량이 1 이상이며 최대 구매 수량을 초과하지 않는지 검증한다. |
| `addQuantity`    | `quantity`, `maxLimit`                    | `void`     | 기존 장바구니 항목의 수량을 증가시킨다. 증가된 최종 수량이 최대 구매 수량을 초과하지 않아야 한다.                      |
| `updateQuantity` | `quantity`, `maxLimit`                    | `void`     | 장바구니 항목의 수량을 새로운 값으로 변경한다. 변경 수량은 1 이상이어야 하며 최대 구매 수량을 초과할 수 없다.              |

---

### 2.2 `OrderSheet` (Aggregate Root)

주문 생성 전에 주문에 필요한 상품, 배송지, 쿠폰, 포인트 등의 정보를 하나의 주문 단위로 관리하고, 할인 및 결제 금액을 계산하며 주문서의 유효성을 보장한다.

### 2.2.1 속성 (Attribute)

| 필드명             | 타입                   | 설명                       |
|-----------------|----------------------|--------------------------|
| id              | Long                 | 주문서 식별자                  |
| orderer         | Orderer              | 주문자 정보                   |
| shippingAddress | ShippingAddress      | 배송 정보                    |
| items           | List<OrderSheetItem> | 주문서에 포함된 상품 항목 목록        |
| cartCoupon      | CartCouponSnapshot   | 주문서에 적용된 장바구니 쿠폰 정보의 스냅샷 |
| usedPoints      | Money                | 주문서에서 사용한 포인트            |
| expiresAt       | LocalDateTime        | 주문서 만료 시각                |

### 2.2.2 핵심 도메인 규칙 (Invariants / Business Rules)

- 규칙 1: 주문서는 하나 이상의 주문 항목을 가져야 한다.
    1. 주문서 생성시 `items`(주문 항목) 이 비어있으면 주문서를 생성할 수 없다.
- 규칙 2: 주문서 생성 시 사용 포인트는 0으로 시작한다.
    1. 주문서가 생성되면 `usedPoints`는 `Money.ZERO`로 초기화된다.
- 규칙 3: 사용 포인트는 현재 주문서에서 사용할 수 있는 최대 포인트를 초과할 수 없다.
    1. 포인트 적용 시 상품 쿠폰 및 장바구니 쿠폰이 반영된 포인트 적용 가능 금액을 기준으로 최대 사용 가능 포인트를 계산하며, 이를 초과한 포인트는 사용할 수 없다.
- 규칙 4: 상품 쿠폰을 적용하면 기존 사용 포인트가 새로운 최대 사용 가능 포인트를 초과하지 않도록 조정한다.
    1. 상품 쿠폰 적용으로 포인트 사용 가능 금액이 감소한 경우 기존 `usedPoints`가 허용 범위를 초과하면 최대 사용 가능 포인트까지 자동으로 조정한다.
- 규칙 5: 장바구니 쿠폰은 최소 결제 금액 조건을 만족하는 경우에만 적용할 수 있다.
    1. 주문 항목의 소계가 장바구니 쿠폰의 `minimumPaymentAmount`보다 작으면 장바구니 쿠폰을 적용할 수 없다.
- 규칙 6: 동일한 상품 쿠폰을 여러 주문 항목에 중복 적용할 수 없다.
    1. 현재 적용하려는 상품 쿠폰이 다른 주문 항목에 이미 적용되어 있다면 다시 적용할 수 없다.
- 규칙 7: 주문서의 사용 포인트는 결제 금액 계산에 반영된다.
    1. 최종 결제 금액은 상품 할인과 상품 쿠폰 할인, 장바구니 쿠폰 할인을 차감한 금액에서 사용 포인트를 추가로 차감하여 계산한다.
- 규칙 8: 주문서는 만료 시각을 기준으로 만료 여부를 판단한다.
    1. 현재 시각이 `expiresAt` 이후이면 주문서는 만료된 것으로 판단한다.

### 2.2.3 주요 행위 (Behavior / Commands)

| 메서드명/행위                            | 파라미터                                                            | 반환값          | 비지니스 의도 및 제약                                                                                                                         |
|------------------------------------|-----------------------------------------------------------------|--------------|--------------------------------------------------------------------------------------------------------------------------------------|
| `create`                           | `CreateOrderSheetContext`, `IdGenerator`                        | `OrderSheet` | 주문서를 생성한다. 식별자를 생성하고 주문 항목이 하나 이상 존재하는지 검증한 후 주문 항목을 생성한다. 생성 시 사용 포인트는 0으로 초기화한다.                                                   |
| `changeShippingAddress`            | `ShippingAddress`                                               | `void`       | 주문서의 배송지를 변경한다. 변경할 배송 정보가 반드시 존재해야 한다                                                                                               |
| `applyItemCoupon`                  | `orderSheetItemId`   , `ItemCouponSnapshot`, `PointUsagePolicy` | `void`       | 특정 주문 항목에 상품 쿠폰을 적용한다. 대상 주문 항목이 존재해야 하며, 동일한 상품 쿠폰이 다른 항목에 이미 적용되어 있으면 적용할 수 없다. 쿠폰 적용 후 포인트 사용 한도를 다시 계산하여 기존 사용 포인트를 필요에 따라 조정한다. |
| `applyCartCoupon`                  | `CartCouponSnapshot`, `PointUsagePolicy`                        | `void`       | 주문서 전체에 장바구니 쿠폰을 적용한다. 주문 항목 소계가 쿠폰의 최소 결제 금액 조건을 만족해야 하며, 적용 후 포인트 사용 한도를 다시 계산한다.                                                  |
| `applyPoints`                      | `Money`, `PointUsagePolicy`                                     | `void`       | 주문서에서 사용할 포인트를 설정한다. 요청한 포인트가 현재 주문서의 최대 사용 가능 포인트를 초과하면 적용할 수 없다.                                                                   |
| `calculateMaxUsablePoints`         | `PointUsagePolicy`                                              | `Money`      | 상품 쿠폰과 장바구니 쿠폰이 적용된 최종 금액을 기준으로 주문서에서 사용할 수 있는 최대 포인트를 계산한다.                                                                         |
| `calculateCartCouponDiscount`      | 없음                                                              | `Money`      | 적용된 장바구니 쿠폰의 할인 금액을 계산한다. 쿠폰이 없으면 0원을 반환하며, 할인 금액이 최종 상품 금액을 초과하지 않도록 제한한다.                                                          |
| `calculateTotalOriginalAmount`     | 없음                                                              | `Money`      | 주문 항목의 원래 가격 기준 총 상품 금액을 계산한다.                                                                                                       |
| `calculateTotalItemDiscount`       | 없음                                                              | `Money`      | 주문 항목의 상품 자체 할인 금액 총합을 계산한다.                                                                                                         |
| `calculateTotalItemCouponDiscount` | 없음                                                              | `Money`      | 주문 항목에 적용된 상품 쿠폰 할인 금액의 총합을 계산한다.                                                                                                    |
| `calculateTotalPaymentAmount`      | 없음                                                              | `Money`      | 상품 할인, 상품 쿠폰, 장바구니 쿠폰, 사용 포인트를 모두 반영한 최종 결제 금액을 계산한다.                                                                                |
| `isExpired`                        | `LocalDateTime`, `currentTime`                                  | `boolean`    | 현재 시각이 주문서의 만료 시각 이후인지 판단하여 주문서 만료 여부를 반환한다.                                                                                         |
| `calculateRemainingTtl`            | `LocalDateTime`, `currentTime`                                  | `Duration`   | 현재 시각부터 주문서 만료 시각까지의 남은 유효 시간을 계산한다. 만료된 경우 0을 반환한다.                                                                                 |
| `hasCoupon`                        | 없음                                                              | `boolean`    | 주문서에 장바구니 쿠폰이 적용되어 있는지 확인한다.                                                                                                         |
| `validateCartCouponNotChanged`     | `CartCouponSnapshot`                                            | `void`       | 주문서에 저장된 장바구니 쿠폰과 현재 쿠폰 정보를 비교하여 쿠폰 정책이 변경되지 않았는지 검증한다. 쿠폰 ID가 다르거나 할인 정책 또는 최소 결제 금액이 변경되면 검증에 실패한다.                                |
| `validatePointsLimit`              | `Money`, `PointUsagePolicy`                                     | `void`       | 요청한 포인트가 현재 주문서에서 사용할 수 있는 최대 포인트를 초과하는지 검증한다.                                                                                       |

### 2.2.4 내부 구성 요소(Entities & Value Objects)

#### 하위 엔티티: 주문서 항목 (`OrderSheetItem`)

주문서에 포함된 개별 상품의 가격, 수량, 옵션 및 상품 쿠폰 스냅샷을 관리하고 해당 상품의 할인 금액과 최종 금액을 계산한다.

**속성 (Attribute)**

| 필드명                | 타입                          | 설명                     |
|--------------------|-----------------------------|------------------------|
| id                 | Long                        | 주문서 항목 식별자             |
| productSnapshot    | ProductSnapshot             | 주문 시점의 상품 정보 스냅샷       |
| priceSnapshot      | ProductPriceSnapshot        | 주문 시점의 상품 가격 정보 스냅샷    |
| itemCouponSnapshot | ItemCouponSnapshot          | 주문 항목에 적용된 상품 쿠폰 스냅샷   |
| quantity           | int                         | 주문 상품 수량               |
| optionSnapshots    | List<ProductOptionSnapshot> | 주문 시점의 상품 옵션 정보 스냅샷 목록 |

**도메인 규칙**

- 규칙 1: 주문 항목의 수량은 1개 이상이어야 한다.
    1. 주문 항목 생성 시 수량이 0 이하이면 생성할 수 없다.
- 규칙 2: 상품 쿠폰이 적용되지 않은 경우 상품 쿠폰 할인 금액은 0원이다.
    1. 상품 쿠폰이 존재하지 않으면 `calculateCouponDiscount()`는 0원을 반환한다.
- 규칙 3: 상품 쿠폰 할인 금액은 상품 금액을 초과할 수 없다.
    1. 계산된 쿠폰 할인 금액이 상품의 할인 가격 총액보다 크더라도 상품 금액을 초과하는 할인은 적용하지 않는다.
- 규칙 4: 상품 쿠폰은 쿠폰 적용 가능 수량까지만 적용된다.
    1. 상품 쿠폰의 `applyQuantityLimit`과 주문 수량 중 작은 값을 실제 할인 적용 수량으로 사용한다.
- 규칙 5: 주문서에 저장된 상품 가격과 현재 상품 가격이 다르면 가격 변경으로 판단한다.
    1. 주문 시점의 `priceSnapshot`과 현재 가격 스냅샷이 동일하지 않으면 가격이 변경된 것으로 검증한다.
- 규칙 6: 주문서에 저장된 상품 쿠폰과 현재 상품 쿠폰의 정책이 다르면 쿠폰 정책 변경으로 판단한다.
    1. 쿠폰 ID뿐만 아니라 할인 정책과 적용 가능 수량 제한이 동일한지 검증한다.

**주요 행위**

| 메서드명/행위                          | 파라미터                                         | 반환값              | 비지니스 의도 및 제약                                                                 |
|----------------------------------|----------------------------------------------|------------------|------------------------------------------------------------------------------|
| `create`                         | `CreateOrderSheetItemContext`, `IdGenerator` | `OrderSheetItem` | 주문서 항목을 생성한다. 식별자를 생성하고 상품 수량이 1개 이상인지 검증한 후 주문 시점의 상품·가격·옵션 정보를 스냅샷으로 보관한다. |
| `applyItemCoupon`                | `ItemCouponSnapshot`                         | `void`           | 주문서 항목에 상품 쿠폰을 적용한다. 적용할 쿠폰 정보가 반드시 존재해야 한다.                                 |
| `removeItemCoupon`               | 없음                                           | `void`           | 주문서 항목에 적용된 상품 쿠폰을 제거한다.                                                     |
| `calculateOriginalLineTotal`     | 없음                                           | `Money`          | 원래 상품 가격을 수량만큼 계산하여 해당 주문 항목의 원가 총액을 반환한다.                                   |
| `calculateItemDiscountLineTotal` | 없음                                           | `Money`          | 상품 자체 할인 금액을 수량만큼 계산한다.                                                      |
| `calculateLineTotal`             | 없음                                           | `Money`          | 상품의 할인 적용 가격을 수량만큼 계산하여 쿠폰 적용 전 상품 금액을 반환한다.                                 |
| `calculateCouponDiscount`        | 없음                                           | `Money`          | 적용된 상품 쿠폰의 총 할인 금액을 계산한다. 쿠폰 적용 수량 제한을 반영하며 상품 금액을 초과하는 할인은 허용하지 않는다.        |
| `calculateFinalAmount`           | 없음                                           | `Money`          | 상품 금액에서 상품 쿠폰 할인 금액을 차감하여 해당 항목의 최종 금액을 계산한다.                                |
| `hasCoupon`                      | 없음                                           | `boolean`        | 주문 항목에 상품 쿠폰이 적용되어 있는지 확인한다.                                                 |
| `validatePriceNotChanged`        | `ProductPriceSnapshot`                       | `void`           | 주문 시점의 가격과 현재 가격을 비교하여 가격 변경 여부를 검증한다.                                       |
| `validateItemCouponNotChanged`   | `ItemCouponSnapshot`                         | `void`           | 주문서에 저장된 상품 쿠폰과 현재 쿠폰을 비교하여 쿠폰 ID, 할인 정책, 적용 수량 제한의 변경 여부를 검증한다.             |

#### 값 객체: 장바구니 쿠폰 스냅샷 (`CartCouponSnapshot`)

주문서 작성 시점의 장바구니 쿠폰 정보를 보존하여 이후 쿠폰 정책이 변경되더라도 당시 주문서에 적용된 쿠폰 정보를 기준으로 할인 금액을 계산하고 변경 여부를 검증한다.

**속성 (Attribute)**

| 필드명                  | 타입                   | 설명                 |
|----------------------|----------------------|--------------------|
| cartCouponId         | Long                 | 장바구니 쿠폰 식별자        |
| name                 | String               | 장바구니 쿠폰 이름         |
| discountPolicy       | CouponDiscountPolicy | 쿠폰 할인 정책           |
| minimumPaymentAmount | Money                | 쿠폰 적용을 위한 최소 결제 금액 |

**도메인 규칙**

- 규칙 1: 쿠폰 식별자는 필수이다.
- 규칙 2: 쿠폰 이름은 비어 있을 수 없다.
- 규칙 3: 할인 정책은 필수이다.
- 규칙 4: 최소 결제 금액은 필수이다.
- 규칙 5: 쿠폰은 기준 금액이 최소 결제 금액 이상인 경우에만 적용할 수 있다.

**주요 행위**

| 메서드명/행위             | 파라미터    | 반환값       | 비지니스 의도 및 제약                        |
|---------------------|---------|-----------|-------------------------------------|
| `isSatisfiedBy`     | `Money` | `boolean` | 기준 금액이 쿠폰의 최소 결제 금액 조건을 만족하는지 판단한다. |
| `calculateDiscount` | `Money` | `Money`   | 쿠폰의 할인 정책을 기준으로 할인 금액을 계산한다.        |

#### 값 객체: 상품 쿠폰 스냅샷 (`ItemCouponSnapshot`)

주문서 작성 시점의 상품 쿠폰 정보를 보존하고, 상품 수량과 쿠폰 적용 수량 제한을 기준으로 실제 할인 금액을 계산한다.

**속성 (Attribute)**

| 필드명                | 타입                   | 설명                    |
|--------------------|----------------------|-----------------------|
| itemCouponId       | Long                 | 상품 쿠폰 식별자             |
| name               | String               | 상품 쿠폰 이름              |
| discountPolicy     | CouponDiscountPolicy | 쿠폰 할인 정책              |
| applyQuantityLimit | Integer              | 쿠폰을 적용할 수 있는 최대 상품 수량 |

**도메인 규칙**

- 규칙 1: 쿠폰 식별자는 필수이다.
- 규칙 2: 쿠폰 이름은 비어 있을 수 없다.
- 규칙 3: 할인 정책은 필수이다.
- 규칙 4: 쿠폰 적용 가능 수량은 필수이다.
- 규칙 5: 상품 쿠폰 할인은 실제 상품 수량과 쿠폰 적용 가능 수량 중 작은 수량에 대해서만 적용된다.

**주요 행위**

| 메서드명/행위                  | 파라미터           | 반환값     | 비지니스 의도 및 제약                                                                 |
|--------------------------|----------------|---------|------------------------------------------------------------------------------|
| `calculateTotalDiscount` | `Money`, `int` | `Money` | 상품 가격과 주문 수량을 기준으로 상품 쿠폰의 총 할인 금액을 계산한다. 쿠폰 적용 가능 수량을 초과하는 상품 수량에는 할인하지 않는다. |

#### 정책: 포인트 정책 (`PointUsagePolicy`)

주문서의 포인트 적용 가능 금액을 기준으로 주문에 사용할 수 있는 최대 포인트를 계산한다.

**주요 행위**

| 메서드명/행위                    | 파라미터    | 반환값     | 비지니스 의도 및 제약                            |
|----------------------------|---------|---------|-----------------------------------------|
| `calculateAvailablePoints` | `Money` | `Money` | 포인트 적용 대상 금액을 기준으로 사용 가능한 최대 포인트를 계산한다. |

#### 정책: 쿠폰 정책 (`CouponDiscountPolicy`)

쿠폰 스냅샷의 쿠폰 할인 금액을 계산한다.

**주요 행위**

| 메서드명/행위             | 파라미터    | 반환값     | 비지니스 의도 및 제약                      |
|---------------------|---------|---------|-----------------------------------|
| `calculateDiscount` | `Money` | `Money` | 쿠폰 적용 대상 금액을 기준으로 쿠폰 할인 금액을 계산한다. |

### 주문(Order)

사용자의 주문 데이터를 저장하는 주문 애거리거트 루트.

#### 속성

| 필드명               | 타입                | 설명         |
|-------------------|-------------------|------------|
| id                | Long              | 주문 아이디     |
| status            | OrderStatus       | 주문 상태      |
| orderName         | String            | 주문 이름      |
| orderer           | Orderer           | 주문자 정보     |
| shippingAddress   | ShippingAddress   | 배송 정보      |
| orderItems        | List<OrderItem>   | 주문 항목      |
| appliedCartCoupon | AppliedCartCoupon | 적용 장바구니 쿠폰 |
| orderAmount       | OrderAmount       | 주문 가격 정보   |
| orderCancelInfo   | OrderCancelInfo   | 주문 취소 사유   |
| createdAt         | LocalDateTime     | 주문 생성일     |

#### 행위

- create(주문 생성): 주문을 생성한다.
- paid(주문 결제): 주문을 결제 상태로 변경한다.

#### 규칙

- 주문을 생성할때 주문 필수 정보(id, status, orderName, orderer, shippingAddress, orderItems, orderAmount, createdAt)가 필요하다.
- 생성된 주문은 결제 대기(`PENDING`) 상태이다.
- 주문을 결제 상태로 변경할때 주문의 상태는 결제 대기(`PENDING`) 상태여야 한다.

### 주문 항목(OrderItem)

주문(Order)의 주문 항목 정보를 저장하는 엔티티

#### 속성

| 필드명               | 타입                          | 설명          |
|-------------------|-----------------------------|-------------|
| id                | Long                        | 주문 항목 아이디   |
| order             | Order                       | 주문          |
| product           | ProductSnapshot             | 상품 정보       |
| productPrice      | ProductPriceSnapshot        | 상품 가격 정보    |
| appliedItemCoupon | AppliedItemCoupon           | 적용 상품 쿠폰    |
| quantity          | Integer                     | 주문 수량       |
| options           | List<ProductOptionSnapshot> | 상품 옵션       |
| orderItemAmount   | OrderItemAmount             | 주문 항목 가격 정보 |

#### 행위

- create(주문 항목 생성): 주문 항목을 생성한다.

#### 규칙

- 주문 항목을 생성할 때 상품 스냅샷이 필요하다.
- 주문 항목을 생성할 때 주문 시점의 상품 가격 정보가 필요하다.
- 주문 항목을 생성할 때 주문 수량이 필요하다.
- 주문 항목을 생성할 때 주문 수량은 1개 이상이어야 한다.
- 주문 항목을 생성할 때 상품 옵션 정보는 빈 목록일 수 있지만 `null`일 수 없다.
- 주문 항목을 생성할 때 주문 항목 가격 정보가 필요하다.

### 결제(Payment)

주문에 대한 결제 정보를 저장하는 엔티티

#### 속성

| 필드명         | 타입              | 설명       |
|-------------|-----------------|----------|
| id          | Long            | 결제 아이디   |
| orderId     | Long            | 주문 아이디   |
| userId      | Long            | 유저 아이디   |
| status      | PaymentStatus   | 결제 상태    |
| method      | PaymentMethod   | 결제 방식    |
| provider    | PaymentProvider | 결제사      |
| paymentKey  | String          | 결제 키     |
| totalAmount | Money           | 결제한 금액   |
| approvedAt  | LocalDateTime   | 결제 승인 시간 |
| failure     | PaymentFailure  | 결제 실패 사유 |

#### 행위

- create(결제 생성): 결제를 생성한다.
- approvePending(결제 승인 대기): 결제를 승인 대기 상태로 변경한다.
- approve(결제 승인): 결제를 승인한다.
- abort(결제 실패): 결제를 실패 처리한다.

#### 규칙

- 결제를 생성할 때 주문 번호가 필요하다.
- 결제를 생성할 때 유저 아이디가 필요하다.
- 결제를 생성할 때 결제 금액이 필요하다.
- 결제를 생성할 때 결제 금액이 0원이면 결제 상태는 결제 완료(`DONE`) 상태이다.
- 생성된 결제는 준비(`READY`) 상태이다.
- 결제를 승인 대기 상태로 변경할때 결제 상태는 준비(`READY`)상태여야 한다.
- 결제를 승인 대기 상태로 변경할때 승인 금액은 결제 금액과 동일해야한다.
- 결제를 승인 대기 상태로 변경할때 지원하지 않는 결제사인 경우 승인 대기로 변경할 수 없다.
- 결제를 승인 대기로 변경하면 결제 상태는 승인 대기로 변경되고 결제사와 결제 키가 초기화 된다.
- 결제를 승인할 때 결제 방식이 필요하다.
- 결제를 승인할 때 결제사가 필요하다.
- 결제를 승인할 때 결제 키가 필요하다.
- 결제를 승인할 때 결제 트랜잭션 키가 필요하다.
- 결제를 승인할 때 승인 가격이 필요하다.
- 결제를 승인할 때 승인 시간이 필요하다.
- 결제를 승인할때 결제는 승인 대기 상태여야 한다.
- 결제를 승인할때 가상 계좌 결제는 지원하지 않는다.
- 결제를 실패 처리할때 실패 사유가 필요하다.
- 결제를 실패 처리할때 결제의 상태는 준비(`READY`) 또는 승인 대기(`APPROVAL_PENDING`) 이어야 한다.

### 결제 거래 내역(PaymentTransaction)

결제에 대한 거래 내역을 저장하는 엔티티

#### 속성

| 필드명            | 타입              | 설명                    |
|----------------|-----------------|-----------------------|
| id             | Long            | 결제 내역 아이디             |
| payment        | Payment         | 결제                    |
| transactionKey | String          | 고유 거래 키               |
| type           | TransactionType | 거래 종류                 |
| amount         | Money           | 거래(승인 또는 취소)에서 변동된 금액 |
| reason         | String          | 거래 발생 사유              |
| occurredAt     | LocalDateTime   | 거래 발생 일시              |

#### 행위

- 결제 승인 내역 저장: 결제 승인 내역을 저장한다.

#### 규칙

### 주문 사가(OrderSaga)

주문 자원 정보를 저장하는 엔티티

#### 속성

| 필드명           | 타입               | 설명       |
|---------------|------------------|----------|
| id            | Long             | 사가 아이디   |
| orderId       | Long             | 주문 아이디   |
| status        | SagaStatus       | 사가 상태    |
| currentStep   | SagaStep         | 현재 진행 단계 |
| payload       | OrderSagaPayload | 주문 자원    |
| failureReason | String           | 실패 사유    |
| version       | Long             | 낙관적 락 버전 |
| createdAt     | LocalDateTime    | 생성일      |
| updatedAt     | LocalDateTime    | 수정일      |

#### 행위

- create(주문 사가 생성): 주문 사가를 생성한다.
- completeForward(스텝을 완료한다): 주문 사가 스텝을 완료한다
- failForward(스텝을 실패한다): 주문 사가 스텝을 실패 처리한다.
- completeCompensate(보상을 완료한다: 주문사가 보상을 완료한다.

#### 규칙

- 주문 사가를 생성할때 주문 아이디는 필수이다.
- 주문 사가를 생성할때 페이로드는 필수이다.
- 주문 사가를 생성하면 상태는 PROCESSING, 단계는 INVENTORY 이다.
- 주문 사가를 생성하면 사가 작업(SagaExecution) 을 추가한다.
- 주문 사가 스텝을 완료하면 해당 execution의 상태를 완료 처리한다.

### 주문 사가 작업(OrderSagaExecution)

#### 속성

| 필드명       | 타입              | 설명          |
|-----------|-----------------|-------------|
| id        | Long            | 작업 아이디      |
| orderSaga | OrderSaga       | 주문 사가 아이디   |
| status    | ExecutionStatus | 주문 사가 작업 상태 |
| type      | ExecutionType   | 작업 종류       |
| step      | SagaStep        | 사가 스텝       |
| createdAt | LocalDateTime   | 생성일         |
| updatedAt | LocalDateTime   | 수정일         |

#### 행위

- create(작업 생성): 사가 작업을 생성한다.
- complete(작업 완료): 사가 작업을 완료한다.
- fail(작업 실패): 사가 작업을 실패로 변경한다.

#### 규칙

- 사가 작업을 생성할때 상태는 PENDING, 타입은 FORWARD 이다.
- 사가 작업을 완료할때 상태는 FAIL일 수 없다.
- 사가 작업을 실패할때 상태는 SUCCESS일 수 없다.

---

## 값 객체(VO)

### 1. 금액(Money)

금액 정보를 관리하는 값 객체

### 2. 주문자(Orderer)

주문을 생성한 주문자 정보

#### 속성

| 필드명         | 타입     | 설명              |
|-------------|--------|-----------------|
| userId      | Long   | 주문을 생성한 유저 아이디  |
| userName    | String | 주문을 생성한 유저 이름   |
| phoneNumber | String | 주문을 생성한 유저 전화번호 |

#### 행위

- 주문자 생성 : 주문자 정보를 생성한다.

#### 규칙

- 주문자 정보를 생성할때 주문자 아이디가 필요하다.
- 주문자 정보를 생성할때 주문자 이름이 필요하다.
- 주문자 정보를 생성할때 주문자 전화번호가 필요하다,.
- 주문자 전화번호는 올바른 형식이여야 한다.

### 3. 배송 정보(ShippingAddress)

주문 배송 정보

| 필드명           | 타입     | 설명        |
|---------------|--------|-----------|
| receiverName  | String | 수령인 이름    |
| receiverPhone | String | 수령인 전화번호  |
| zipCode       | String | 우편번호      |
| address       | String | 배송지 주소    |
| addressDetail | String | 배송지 상세 주소 |

#### 행위

- 배송정보 생성: 배송정보를 생성한다.

#### 규칙

- 배송정보를 생성할때 수령인 이름이 필요하다.
- 배송정보를 생성할때 수령인 전화번호가 필요하다.
- 배송정보를 생성할때 우편변호가 필요하다.
- 배송정보를 생성할때 주소가 필요하다.
- 배송정보를 생성할때 상세주소가 필요하다.
- 수령인 전화번호는 올바른 형식이여야 한다.
- 우편 번호는 올바른 형식이여야 한다.

### 4-1. 상품 쿠폰(ItemCouponSnapshot)

주문 적용 상품 쿠폰 정보

#### 속성

| 필드명                | 타입                   | 설명        |
|--------------------|----------------------|-----------|
| itemCouponId       | Long                 | 적용 쿠폰 아이디 |
| name               | String               | 쿠폰 이름     |
| discountPolicy     | CouponDiscountPolicy | 쿠폰 할인 정책  |
| applyQuantityLimit | int                  | 적용 가능 수량  |

#### 행위

- 상품 쿠폰 할인 금액을 계산한다

#### 규칙

- 주문 항목의 수량이 쿠폰 적용 가능 수량을 초과하는 경우 쿠폰의 최대 적용 가능 수량을 기준으로 계산된다.

### 4-2. 장바구니 쿠폰(CartCouponSnapshot)

주문서(OrderSheet) 단계에서 장바구니 쿠폰의 유효성을 검증하고 할인 금액을 동적으로 계산하기 위한 스냅샷 정보.

#### 속성

| 필드명                  | 타입                   | 설명        |
|----------------------|----------------------|-----------|
| cartCouponId         | Long                 | 적용 쿠폰 아이디 |
| name                 | String               | 쿠폰 이름     |
| discountPolicy       | CouponDiscountPolicy | 쿠폰 할인 정책  |
| minimumPaymentAmount | Money                | 최소 결제 금액  |

#### 행위

- 장바구니 쿠폰 적용 가능 여부를 검증한다.
- 장바구니 쿠폰 할인 금액을 계산한다.

### 5. 상품 정보(ProductSnapshot)

주문 시점의 상품 정보

### 속성

| 필드명              | 타입     | 설명        |
|------------------|--------|-----------|
| productId        | Long   | 상품 아이디    |
| productVariantId | Long   | 상품 변형 아이디 |
| sku              | String | 상품 SKU    |
| productName      | String | 상품 이름     |           
| thumbnail        | String | 상품 대표 이미지 |

### 6. 상품 가격 정보(ProductPriceSnapshot)

상품 가격 관련 정보

| 필드명             | 타입      | 설명       |
|-----------------|---------|----------|
| originalPrice   | Money   | 상품 정상가   |
| discountRate    | Integer | 상품 할인율   |
| discountAmount  | Money   | 상품 할인 가격 |
| discountedPrice | Money   | 상품 판매가   |

### 7. 상품 옵션(ProductOptionSnapshot)

주문 시점의 상품 옵션 정보

### 속성

| 필드명             | 타입     | 설명    |
|-----------------|--------|-------|
| optionTypeName  | String | 옵션 이름 |
| optionValueName | String | 옵션 값  |

### 8. 적용 장바구니 쿠폰 정보(AppliedCartCoupon)

주문(Order)에 실제 적용이 완료된 장바구니 쿠폰의 정보.

### 속성

| 필드명          | 타입     | 설명          |
|--------------|--------|-------------|
| cartCouponId | Long   | 장바구니 쿠폰 아이디 |
| name         | String | 장바구니 쿠폰 이름  |

### 9. 주문 가격 정보(OrderAmount)

주문 가격 요약 정보

| 필드명                     | 타입    | 설명            |
|-------------------------|-------|---------------|
| totalOriginalAmount     | Money | 총 주문 상품 원 가격  |
| totalItemDiscount       | Money | 총 주문 상품 할인 가격 |
| totalItemCouponDiscount | Money | 총 상품 쿠폰 할인 가격 |
| cartCouponDiscount      | Money | 장바구니 쿠폰 할인 가격 |
| usedPoints              | Money | 적용 포인트        |
| totalPaymentAmount      | Money | 최종 결제 금액      |

### 10. 주문 취소 사유(OrderCancelInfo)

주문 취소 정보

### 속성

| 필드명        | 타입            | 설명     |
|------------|---------------|--------|
| reason     | String        | 취소 사유  |
| canceledAt | LocalDateTime | 주문 취소일 |

### 11. 적용 상품 쿠폰 정보(AppliedItemCoupon)

주문 항목(OrderItem)에 실제 적용이 완료된 상품 쿠폰의 정보.

### 속성

| 필드명          | 타입     | 설명        |
|--------------|--------|-----------|
| itemCouponId | Long   | 상품 쿠폰 아이디 |
| name         | String | 상품 쿠폰 이름  |

### 12. 주문 항목 가격 정보(OrderItemAmount)

### 속성

| 필드명                | 타입    | 설명             |
|--------------------|-------|----------------|
| originalAmount     | Money | 항목 원가 총액       |
| itemDiscount       | Money | 항목 상품 할인 총액    |
| lineTotal          | Money | 상품 판매가 총액      |
| itemCouponDiscount | Money | 항목 상품 쿠폰 할인 금액 |
| finalAmount        | Money | 항목 최종 결제 금액    |

### 13. 결제 취소 사유(PaymentFailure)

### 속성

| 필드명     | 타입     | 설명     |
|---------|--------|--------|
| code    | String | 실패 코드  |
| message | String | 실패 메시지 |

### 14. 주문 사가 페이로드(OrderSagaPayload)

### 속성

| 필드명         | 타입              | 설명       |
|-------------|-----------------|----------|
| userId      | Long            | 유저 아이디   |
| orderLines  | List<OrderLine> | 주문 항목    |
| usedCoupons | UsedCoupons     | 사용 쿠폰 정보 |
| usedPoints  | Money           | 사용 포인트   |

--

## 도메인 정책

### 1-1. 정액 할인 쿠폰 정책(FixedCouponDiscountPolicy)

정액 할인 쿠폰에 대한 정책

#### 속성

| 필드명            | 타입    | 설명    |
|----------------|-------|-------|
| discountAmount | Money | 할인 금액 |

#### 행위

- 할인 금액을 계산한다

#### 규칙

- 대상 금액의 크기와 상관없이 지정된 고정 금액이 할인 금액이다.

### 1-2. 정률 할인 쿠폰 정책(RateCouponDiscountPolicy)

정률 할인 쿠폰에 대한 정책

#### 속성

| 필드명               | 타입    | 설명           |
|-------------------|-------|--------------|
| discountRate      | int   | 할인 비율        |
| maxDiscountAmount | Money | 쿠폰의 최대 할인 금액 |

#### 행위

- 할인 금액을 계산한다.

#### 규칙

- 대상 금액에 지정된 비율을 곱하여 할인 금액이 계산된다.
- 할인 금액은 최대 할인 금액을 초과할 수 없다.
- 할인 금액이 최대 할인 금액을 초과하는 경우, 할인 금액 한도는 최대 할인 금액으로 적용된다.
- 할인 금액이 1원단위라면 10원단위로 절삭된다.

### 2. 기본 포인트 정책(DefaultPointUsagePolicy)

포인트 사용에 대한 정책

#### 속성

| 필드명       | 타입         | 설명              |
|-----------|------------|-----------------|
| limitRate | BigDecimal | 최대 사용 가능 포인트 비율 |

#### 행위

- 사용 가능 포인트 금액을 계산한다.

#### 규칙

- 할인 비율은 0%(0.0) 에서 100%(1.0) 사이값이여야 한다.