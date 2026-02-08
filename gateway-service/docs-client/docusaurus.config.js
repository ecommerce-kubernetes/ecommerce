import {themes as prismThemes} from 'prism-react-renderer';

const config = {
  title: 'BuyNest Ecommerce',
  tagline: 'BuyNest System Guide',
  favicon: 'img/favicon.ico',

  future: {
    v4: true,
  },
  url: 'https://your-docusaurus-site.example.com',
  baseUrl: '/',

  organizationName: 'facebook',
  projectName: 'docusaurus',

  onBrokenLinks: 'warn',
  onBrokenMarkdownLinks: 'warn',
  themes: ['docusaurus-theme-openapi-docs', '@docusaurus/theme-mermaid'],
  markdown: {
      mermaid: true,
  },

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      ({
        docs: {
          sidebarPath: './sidebars.js',
          docItemComponent: "@theme/ApiItem",
        },
        blog: {
          showReadingTime: true,
          feedOptions: {
            type: ['rss', 'atom'],
            xslt: true,
          },
          onInlineTags: 'warn',
          onInlineAuthors: 'warn',
          onUntruncatedBlogPosts: 'warn',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      }),
    ],
  ],

  plugins: [
    [
      'docusaurus-plugin-openapi-docs',
      {
        id: "api",
        docsPluginId: "classic",
        config: {
          product: {
            specPath: "static/api-specs/product-openapi.json",
            outputDir: "docs/api/product",
          },

          user: {
            specPath: "static/api-specs/user-openapi.json",
            outputDir: "docs/api/user",
            sidebarOptions: {
              groupPathsBy: "tag",
              categoryLinkSource: "tag",
            },
          },

          order: {
            specPath: "static/api-specs/order-openapi.json",
            outputDir: "docs/api/order",
            sidebarOptions: {
              groupPathsBy: "tag",
              categoryLinkSource: "tag",
            },
          }
        }
      },
    ],

    function (context, options) {
      return {
        name: 'custom-docusaurus-plugin',
        configureWebpack(config, isServer, utils) {
          return {
            module: {
              rules: [
                {
                  test: /\.mjs$/,
                  include: /node_modules/,
                  type: 'javascript/auto',
                  resolve: {
                    fullySpecified: false,
                  },
                },
              ],
            },
            resolve: {
              alias: {
                'process/browser': require.resolve('process/browser'),
              },
              fallback: {
                path: require.resolve('path-browserify'),
                url: require.resolve('url/'),
                buffer: require.resolve('buffer/'),
                stream: require.resolve('stream-browserify'),
                process: require.resolve('process/browser'),
              },
            },
            plugins: [
              new (require('webpack').ProvidePlugin)({
                Buffer: ['buffer', 'Buffer'],
                process: 'process/browser',
              }),
            ],
          };
        },
      };
    },
  ],

  themeConfig:
    ({
      image: 'img/docusaurus-social-card.jpg',
      colorMode: {
        respectPrefersColorScheme: true,
      },
      navbar: {
        title: '',
        logo: {
          alt: 'My Site Logo',
          src: 'img/buynest.svg',
        },
        items: [
          {
            type: 'docSidebar',
            sidebarId: 'tutorialSidebar',
            position: 'left',
            label: '개요',
          },
          {
            type: 'docSidebar',
            sidebarId: 'apiSidebar',
            position: 'left',
            label: 'API 테스트',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Community',
            items: [
              {
                label: 'GitHub',
                href: 'https://github.com/minsik2434',
              },
              {
                label: 'Tech Blog',
                href: 'https://velog.io/@minsik2434/posts',
              },
            ],
          },
          {
            title: 'Resources',
            items: [
              {
                label: '깃허브 레포지토리',
                href: 'https://github.com/ecommerce-kubernetes/ecommerce',
              },
              {
                label: '시스템 구조',
                to: '/docs/architecture',
              },
              {
                label: 'API 명세',
                to: '/docs/api-spec',
              },
            ],
          },
          {
           title: 'Contact',
           items: [
             {
               label: 'Email Me',
               href: 'https://mail.google.com/mail/?view=cm&fs=1&to=minsik2434@gmail.com',
             },
           ],
         },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} BuyNest Project.`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
      },
      mermaid: {
        theme: {light: 'neutral', dark: 'forest'},
      },
    }),
};

export default config;
