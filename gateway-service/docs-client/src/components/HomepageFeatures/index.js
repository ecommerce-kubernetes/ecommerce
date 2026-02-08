import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: '이벤트 기반의 비동기 통신',
    image: 'img/icon-kafka.svg',
    description: (
      <>
        <strong>Kafka 기반</strong>의 이벤트 드리븐 아키텍처와 <strong>SAGA 패턴</strong>을 적용하여
        서비스간 비동기 통신으로 주문 처리를 수행하고
        보상 트랜잭션을 통해 분산 환경에서도 <strong>데이터 정합성</strong>을 유지합니다
        이를 통해 <strong>서비스간 결합도</strong>를 낮추어 서비스 단위의 독립적인 확장이 가능하도록 설계하였습니다
      </>
    ),
  },
  {
    title: '인증과 장애 전파',
    image: 'img/icon-shield.svg',
    description: (
      <>
        <strong>Spring Cloud Gateway</strong>을 통해 JWT 인증/인가를 중앙화 하여,
        각 서비스의 인증 책임을 분리하고 보안 처리를 일관적으로 관리하였습니다
        또한 <strong>Resilience4j</strong>를 적용하여 특정 마이크로 서비스의 장애가
        전체 시스템으로 전파되는것을 차단하고 Fallback 처리를 통해 시스템의 가용성과 장애 내성을 높였습니다
      </>
    )
  },
  {
    title: '서비스 디스커버리와 설정',
    image: 'img/cloud.svg',
    description: (
      <>
        <strong>Netflix Eureka</strong>를 통해 서비스 디스커버리를 구성하여
        동적으로 변경되는 서비스 인스턴스를 자동으로 감지하고 안정적인 서비스 호출 환경을 구축했습니다
        또한 <strong>Spring Cloud Config</strong>를 사용하여 수십개의 서비스 설정을 중앙에서 관리하여
        설정의 일관성과 운영 편의성을 향상시켰습니다
      </>
    )
  },
];

function Feature({image, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className={styles.featureCard}>
      <div className={clsx('text--center', styles.featureImageContainer)}>
        {image && (
          <img
            role="img"
            src={image}
            alt={title}
            className={styles.featureImage}
          />
        )}
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3" style={{marginTop: '20px', fontWeight: 'bold'}}>{title}</Heading>
        <p style={{color: '#6b7684'}}>{description}</p>
      </div>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
