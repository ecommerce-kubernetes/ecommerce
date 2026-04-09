package com.example.product_service.support.fixture;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;

public class FixtureMonkeyFactory {
    public static final FixtureMonkey get = FixtureMonkey.builder()
            .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
            .defaultNotNull(true)
            .plugin(new JakartaValidationPlugin())
            .build();

    private FixtureMonkeyFactory() {
    }
}
