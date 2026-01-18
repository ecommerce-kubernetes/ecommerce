package com.example.product_service.support.fixture.builder;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.option.domain.model.OptionType;
import com.example.product_service.api.product.domain.model.*;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductTestBuilder {

    private Long id = 1L;
    private String name = "테스트 상품";
    private Category category = createMockLeafCategory();
    private ProductStatus status = ProductStatus.PREPARING;
    private String description = "테스트 설명";
    private Double rating = 0.0;
    private Long reviewCount = 0L;
    private Double popularityScore = 0.0;
    private Long lowestPrice = 10000L;
    private Long originalPrice = 10000L;
    private Integer maxDiscountRate = 0;
    private List<ProductVariant> variants = new ArrayList<>();
    private List<ProductImage> images = new ArrayList<>();
    private List<OptionType> optionTypes = new ArrayList<>();

    public static ProductTestBuilder aProduct() {
        return new ProductTestBuilder();
    }

    public ProductTestBuilder withId(Long id) {
        this.id = id;
        return this;
    }
    public ProductTestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProductTestBuilder withCategory(Category category) {
        this.category = category;
        return this;
    }

    public ProductTestBuilder withStatus(ProductStatus status) {
        this.status = status;
        return this;
    }

    public ProductTestBuilder withPrice(Double rating, Long reviewCount, Double popularityScore) {
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.popularityScore = popularityScore;
        return this;
    }

    public ProductTestBuilder withPrice(Long displayPrice, Long originalPrice, Integer maxDiscountRate) {
        this.lowestPrice = displayPrice;
        this.originalPrice = originalPrice;
        this.maxDiscountRate = maxDiscountRate;
        return this;
    }

    public ProductTestBuilder withOptions(List<OptionType> optionTypes) {
        this.optionTypes = optionTypes;
        return this;
    }

    public ProductTestBuilder withVariants(List<ProductVariant> variants) {
        this.variants = variants;
        return this;
    }

    // [핵심] 이미지를 강제로 주입
    public ProductTestBuilder withImages(List<ProductImage> images) {
        this.images = images;
        return this;
    }

    public Product build() {
        Product product = Product.create(name, description, category);

        ReflectionTestUtils.setField(product, "id", id);
        ReflectionTestUtils.setField(product, "status", status);
        ReflectionTestUtils.setField(product, "rating", rating);
        ReflectionTestUtils.setField(product, "reviewCount", reviewCount);
        ReflectionTestUtils.setField(product, "popularityScore", popularityScore);

        ReflectionTestUtils.setField(product, "lowestPrice", lowestPrice);
        ReflectionTestUtils.setField(product, "originalPrice", originalPrice);
        ReflectionTestUtils.setField(product, "maxDiscountRate", maxDiscountRate);

        if (!optionTypes.isEmpty()) {
            List<ProductOption> productOptions = new ArrayList<>();
            for (int i = 0; i < optionTypes.size(); i++) {
                ProductOption option = createProductOptionByReflection(product, optionTypes.get(i), i + 1);
                productOptions.add(option);
            }
            ReflectionTestUtils.setField(product, "options", new ArrayList<>(productOptions));
        }

        if (!variants.isEmpty()) {
            ReflectionTestUtils.setField(product, "variants", new ArrayList<>(variants));
        }

        if (!images.isEmpty()) {
            ReflectionTestUtils.setField(product, "images", new ArrayList<>(images));

            if (!this.images.isEmpty()) {
                ReflectionTestUtils.setField(product, "thumbnail", this.images.get(0).getImageUrl());
            }
        }

        if (status == ProductStatus.ON_SALE) {
            ReflectionTestUtils.setField(product, "publishedAt", LocalDateTime.now());
        }

        return product;
    }

    private ProductOption createProductOptionByReflection(Product product, OptionType optionType, int priority) {
        try {
            Constructor<ProductOption> constructor = ProductOption.class.getDeclaredConstructor(OptionType.class, int.class);
            constructor.setAccessible(true);
            ProductOption option = constructor.newInstance(optionType, priority);
            ReflectionTestUtils.setField(option, "product", product);
            return option;
        } catch (Exception e) {
            throw new RuntimeException("테스트용 ProductOption 생성 실패", e);
        }
    }

    private Category createMockLeafCategory() {
        Category mockCategory = Mockito.mock(Category.class);
        Mockito.when(mockCategory.isLeaf()).thenReturn(true);
        return mockCategory;
    }
}
