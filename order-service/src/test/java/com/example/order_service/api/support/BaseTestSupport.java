package com.example.order_service.api.support;

import com.example.order_service.api.support.fixture.FixtureMonkeyFactory;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

@ExtendWith(MockitoExtension.class)
public abstract class BaseTestSupport {
    protected final FixtureMonkey fixtureMonkey = FixtureMonkeyFactory.get;

    protected <T> T nonNull(T obj) {
        return Objects.requireNonNull(obj, "테스트 픽스처 데이터 생성 실패");
    }

    protected <T> T sample(ArbitraryBuilder<T> builder) {
        return nonNull(builder.sample());
    }
}
