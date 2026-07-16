# OrderService 용어 사전

## 도메인 용어

| 용어              | 영문                 | 설명                                                   | 관련 도메인             |
|-----------------|--------------------|------------------------------------------------------|--------------------|
| 장바구니            | Cart               | 사용자가 주문을 위해 상품을 임시로 담아두는 공간                          | Cart               |
| 장바구니 항목         | CartItem           | 장바구니에 담긴 개별 상품                                       | Cart               |
| 주문서             | OrderSheet         | 주문 생성 전 주문 할인 정보, 배송정보, 가격 관련 정보를 계산해 임시로 생성된 주문 데이터 | OrderSheet         |
| 주문 항목           | OrderSheetItem     | 주문서 주문 상품 정보                                         | OrderSheetItem     |
| 주문자             | Orderer            | 주문을 생성한 주문 소유자 정보                                    | Orderer            |
| 배송정보            | ShippingAddress    | 주문 배송 정보                                             | ShippingAddress    |
| 금액              | Money              | 돈을 표현하는 정보                                           | Money              |
| 상품 쿠폰           | ItemCouponSnapshot | 주문 항목에 적용된 상품 단위 쿠폰                                  | ItemCouponSnapshot |
| 정상가             | OriginalPrice      | 상품 할인이 적용되지 않은 상품 원본 가격                              | OrderSheetItem     |
| 판매가             | DiscountedPrice    | 상품 할인이 적용된 상품 판매 가격                                  | OrderSheetItem     |
| 장바구니 쿠폰         | CartCouponSnapshot | 주문에 적용된 주문 단위 쿠폰                                     | CartCouponSnapshot |
| 주문 항목 상품 판매가 총액 | LineTotal          | 주문 항목의 상품 판매가와 주문 수량을 곱한 값                           | OrderSheetItem     |
| 소계              | SubTotal           | 주문의 전체 주문 항목 상품 판매가 총액의 합계                           | OrderSheet         |

### 식별자

| 이름               | 설명          |
|------------------|-------------|
| cartId           | 장바구니 식별자    |
| cartItemId       | 장바구니 항목 식별자 |
| orderSheetId     | 주문서 식별자     |
| orderSheetItemId | 주문 항목 식별자   |
|                  |             |
|                  |             |

### 장바구니 항목 상태

| 상태           | 의미    |
|--------------|-------|
| AVAILABLE    | 주문 가능 |
| NOT_FOR_SALE | 판매 불가 |
| OUT_OF_STOCK | 재고 부족 |

### 쿠폰 할인 

| 용어       | 영문                        | 설명                             | 관려 도메인               |
|----------|---------------------------|--------------------------------|----------------------|
| 쿠폰 할인 정책 | CouponDiscountPolicy      | 쿠폰의 할인 정책을 의미한다.               | CouponDiscountPolicy |
| 정액 할인    | FixedCouponDiscountPolicy | 고정된 금액을 할인하는 쿠폰 정책             | CouponDiscountPolicy |
| 정률 할인    | RateCouponDiscountPolicy  | 상품 판매가를 기준으로 비율 금액을 할인하는 쿠폰 정책 | CouponDiscountPolicy |
