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

  onBrokenLinks: 'throw',
  themes: ['@docusaurus/theme-mermaid'],
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
          {to: '/blog', label: 'Blog', position: 'left'},
          {
            href: 'https://github.com/facebook/docusaurus',
            label: 'GitHub',
            position: 'right',
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
