package com.example.order_service.api.support;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

import java.util.Objects;

public class TestUtil {
    public static <T> T nonNull(T obj) {
        return Objects.requireNonNull(obj, "테스트 픽스처 데이터 생성 실패");
    }

    public static <T> T sample(ArbitraryBuilder<T> builder) {
        return nonNull(builder.sample());
    }
}
