package com.example.order_service.api.support;

import com.example.order_service.api.support.fixture.FixtureMonkeyFactory;
import com.navercorp.fixturemonkey.FixtureMonkey;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class BaseTestSupport {
    protected final FixtureMonkey fixtureMonkey = FixtureMonkeyFactory.get;
}
