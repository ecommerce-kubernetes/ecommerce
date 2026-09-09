package com.example.product_service.product.domain;

import com.example.product_service.category.domain.Category;
import com.example.product_service.common.entity.BaseEntity;
import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.common.exception.ProductErrorCode;
import com.example.product_service.option.domain.OptionType;
import com.example.product_service.product.domain.context.CreateProductContext;
import com.example.product_service.product.domain.vo.RepresentativePrice;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product extends BaseEntity {

    @Id
    private Long id;

    private String name;

    private Long categoryId;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    private String description;

    private LocalDateTime publishedAt;

    private LocalDateTime saleStoppedAt;

    private LocalDateTime deletedAt;

    private String thumbnail;

    private Double rating;

    private Long reviewCount;

    private Double popularityScore;

    @Embedded
    private RepresentativePrice representativePrice;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductDescriptionImage> descriptionImages = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Product(Long id, String name, Long categoryId, ProductStatus status, String description, LocalDateTime publishedAt,
                    LocalDateTime saleStoppedAt, LocalDateTime deletedAt, String thumbnail, Double rating, Long reviewCount,
                    Double popularityScore, RepresentativePrice representativePrice) {
        Assert.notNull(id, "상품 아이디는 필수이다");
        Assert.hasText(name, "상품 이름은 필수이다");
        Assert.notNull(categoryId, "상품 카테고리는 필수이다");
        Assert.notNull(status, "상품 상태는 필수이다");

        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.status = status;
        this.description = description;
        this.publishedAt = publishedAt;
        this.saleStoppedAt = saleStoppedAt;
        this.deletedAt = deletedAt;
        this.thumbnail = thumbnail;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.popularityScore = popularityScore;
        this.representativePrice = representativePrice;
    }

    public static Product create(CreateProductContext context) {
        return Product.builder()
                .id(context.id())
                .name(context.name())
                .categoryId(context.categoryId())
                .status(ProductStatus.PREPARING)
                .description(context.description())
                .rating(0.0)
                .reviewCount(0L)
                .popularityScore(0.0)
                .build();
    }
}
