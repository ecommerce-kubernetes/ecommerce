import type { SidebarsConfig } from "@docusaurus/plugin-content-docs";

const sidebar: SidebarsConfig = {
  apisidebar: [
    {
      type: "doc",
      id: "api/order/buynest-order-api",
    },
    {
      type: "category",
      label: "CART",
      items: [
        {
          type: "doc",
          id: "api/order/02-cart-02-get-list",
          label: "장바구니 목록 조회",
          className: "api-method get",
        },
        {
          type: "doc",
          id: "api/order/02-cart-01-add-cart-item",
          label: "장바구니 상품 추가",
          className: "api-method post",
        },
        {
          type: "doc",
          id: "api/order/02-cart-04-clear",
          label: "장바구니 비우기",
          className: "api-method delete",
        },
        {
          type: "doc",
          id: "api/order/02-cart-03-delete-item",
          label: "장바구니 상품 삭제",
          className: "api-method delete",
        },
        {
          type: "doc",
          id: "api/order/02-cart-05-update-quantity",
          label: "장바구니 상품 수량 변경",
          className: "api-method patch",
        },
      ],
    },
    {
      type: "category",
      label: "Notification",
      items: [
        {
          type: "doc",
          id: "api/order/03-notification-01-connect",
          label: "SSE 연결",
          className: "api-method get",
        },
      ],
    },
    {
      type: "category",
      label: "ORDER",
      items: [
        {
          type: "doc",
          id: "api/order/01-order-04-get-list",
          label: "주문 목록 조회",
          className: "api-method get",
        },
        {
          type: "doc",
          id: "api/order/01-order-01-create",
          label: "주문 생성",
          className: "api-method post",
        },
        {
          type: "doc",
          id: "api/order/01-order-02-confirm",
          label: "결제 승인",
          className: "api-method post",
        },
        {
          type: "doc",
          id: "api/order/01-order-03-get",
          label: "주문 조회",
          className: "api-method get",
        },
      ],
    },
  ],
};

export default sidebar.apisidebar;
