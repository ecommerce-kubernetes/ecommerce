package com.example.product_service.product.fixture;

import com.example.product_service.common.util.IdGenerator;
import com.example.product_service.product.domain.Product;
import com.example.product_service.product.domain.context.CreateProductContext;

import java.util.concurrent.atomic.AtomicLong;

public class ProductFixtureBuilder {

    private static final AtomicLong idSeq = new AtomicLong(100L);
    private static final IdGenerator ID_GENERATOR = idSeq::getAndIncrement;

    private Long categoryId = 1L;
    private String name = "상품";
    private String description = "상품 설명";

    public static ProductFixtureBuilder given() {
        return new ProductFixtureBuilder();
    }

    public ProductFixtureBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProductFixtureBuilder withCategoryId(Long categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public ProductFixtureBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public Product build() {
        CreateProductContext context = CreateProductContext.builder()
                .id(ID_GENERATOR.generate())
                .categoryId(categoryId)
                .name(name)
                .description(description)
                .build();
        return Product.create(context);
    }
}
