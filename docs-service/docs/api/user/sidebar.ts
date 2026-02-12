import type { SidebarsConfig } from "@docusaurus/plugin-content-docs";

const sidebar: SidebarsConfig = {
  apisidebar: [
    {
      type: "doc",
      id: "api/user/buynest-user-api",
    },
    {
      type: "category",
      label: "AUTH",
      items: [
        {
          type: "doc",
          id: "api/user/02-auth-01-login",
          label: "로그인",
          className: "api-method post",
        },
      ],
    },
    {
      type: "category",
      label: "USER",
      items: [
        {
          type: "doc",
          id: "api/user/01-user-01-create",
          label: "유저 생성",
          className: "api-method post",
        },
      ],
    },
  ],
};

export default sidebar.apisidebar;
