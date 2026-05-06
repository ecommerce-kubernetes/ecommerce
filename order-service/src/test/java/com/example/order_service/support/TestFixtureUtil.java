package com.example.order_service.support;

import com.example.order_service.common.domain.vo.Money;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import java.util.Objects;

public class TestFixtureUtil {

    private static final Arbitrary<Money> moneyArbitrary =
            Arbitraries.longs().between(100, 10000000).map(Money::wons);

    public static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
            .defaultNotNull(true)
            .pushExactTypeArbitraryIntrospector(
                    Money.class,
                    context -> new ArbitraryIntrospectorResult(
                            CombinableArbitrary.from(moneyArbitrary::sample)
                    )
            )
            .plugin(new JakartaValidationPlugin())
            .build();

    public static <T> T nonNull(T obj) {
        return Objects.requireNonNull(obj, "테스트 픽스처 데이터 생성 실패");
    }

    public static <T> T sample(ArbitraryBuilder<T> builder) {
        return nonNull(builder.sample());
    }

    public static <T> T giveMeOne(Class<T> type) {
        return nonNull(fixtureMonkey.giveMeOne(type));
    }

}
