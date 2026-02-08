import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';

import Heading from '@theme/Heading';
import styles from './index.module.css';

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
 return (
     <header style={{padding: '120px 0', textAlign: 'center', background: '#f9fafb'}}>
       <div className="container">
         <h1 style={{fontSize: '3rem', fontWeight: '800', marginBottom: '20px'}}>
           <span style={{color: '#A08574'}}>BuyNest</span> <span style={{color: '#2b2b2b'}}>Ecommerce</span>
         </h1>

         <p style={{
             fontSize: '1.2rem',
             color: '#525252',
             marginBottom: '40px',
             lineHeight: '1.8',
             maxWidth: '800px',
             marginLeft: 'auto',
             marginRight: 'auto',
             wordBreak: 'keep-all'
         }}>
           BuyNest는 MSA 기반으로 구성된 <strong>이커머스 시스템</strong> 입니다.<br/>
           각 서비스는 <strong>Spring Cloud</strong> 기반의 마이크로 서비스 아키텍쳐로 구성되어 있으며 <br/>
           서비스 간 비동기 통신은 이벤트 스트리밍 기반으로 구현하였습니다.<br/>
           또한 <strong>Eureka</strong> 기반의 서비스 디스커버리와 <strong>Config Server</strong>를 통한 중앙 설정 관리<br/>
           <strong>Gateway</strong>을 활용한 API 라우팅을 적용하여 <br/>
           실제 프로덕션 환경을 고려한 MSA 설계 및 운영 구조를 구현한 프로젝트 입니다
         </p>

         <div style={{display: 'flex', gap: '15px', justifyContent: 'center'}}>
            <Link
                className="button button--primary button--lg btn-brown btn-animate"
                to="/docs/intro/intro">
                프로젝트 개요 📘
            </Link>
            <div className={styles.dropdownContainer}>
              <button className={clsx("button button--secondary button--lg btn-animate", styles.dropdownBtn)}>
                API 명세서 📝 ▼
              </button>
              <div className={styles.dropdownContent}>
                  <a
                      href="/product-service/docs/index.html"
                      target="_blank"
                      rel="noopener noreferrer"
                      className={styles.dropdownItem}
                  > 상품 서비스 API 명세
                  </a>
                  <a
                      href="/order-service/docs/index.html"
                      target="_blank"
                      rel="noopener noreferrer"
                      className={styles.dropdownItem}
                  > 주문 서비스 API 명세
                  </a>
                  <a
                      href="/user-service/docs/index.html"
                      target="_blank"
                      rel="noopener noreferrer"
                      className={styles.dropdownItem}
                  > 유저 서비스 API 명세
                  </a>
              </div>
            </div>
            <Link
              className="button button--secondary button--lg btn-animate"
              to="/docs/api-test">
               API 테스트 🧪
            </Link>
         </div>
       </div>
     </header>
   );
}

export default function Home() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`Hello from ${siteConfig.title}`}
      description="Description will go into a meta tag in <head />">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}
