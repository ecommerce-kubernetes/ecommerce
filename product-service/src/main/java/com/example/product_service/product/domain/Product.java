package com.example.product_service.product.domain;

import com.example.product_service.common.entity.BaseEntity;
import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.product.domain.context.*;
import com.example.product_service.product.domain.vo.RepresentativePrice;
import com.example.product_service.product.exception.ProductErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

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
    private List<ProductOptionType> productOptionTypes = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductMainImage> mainImages = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductDetailImage> detailImages = new ArrayList<>();

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

    public void update(UpdateProductContext context) {
        Assert.hasText(context.name(), "상품 이름은 필수이다");
        Assert.notNull(context.categoryId(), "상품 카테고리는 필수이다");

        this.name = context.name();
        this.description = context.description();
        this.categoryId = context.categoryId();
    }

    public void registerOptionTypes(List<RegisterOptionTypeContext> contexts) {
        if (this.status == ProductStatus.ON_SALE || this.status == ProductStatus.DELETED) {
            throw new BusinessException(ProductErrorCode.CANNOT_REGISTER_OPTION_TYPE);
        }

        HashSet<Long> optionTypeIdSet = new HashSet<>();
        boolean isDuplicateId = contexts.stream().anyMatch(context -> !optionTypeIdSet.add(context.optionTypeId()));

        if (isDuplicateId) {
            throw new BusinessException(ProductErrorCode.OPTION_TYPE_DUPLICATED);
        }

        //TODO 상품 변형 비활성화

        this.productOptionTypes.clear();

        IntStream.range(0, contexts.size())
                .mapToObj(i -> ProductOptionType.create(contexts.get(i), this, i + 1))
                .forEach(this.productOptionTypes::add);
    }

    public void deleted(LocalDateTime deletedAt) {
        if (this.status == ProductStatus.ON_SALE) {
            throw new BusinessException(ProductErrorCode.CANNOT_DELETE_PRODUCT);
        }

        //TODO 상품 변형 비활성화

        this.status = ProductStatus.DELETED;
        this.deletedAt = deletedAt;
    }

    public void addMainImages(List<AddMainImageContext> contexts) {
        if (this.status == ProductStatus.DELETED) {
            throw new BusinessException(ProductErrorCode.CANNOT_ADD_MAIN_IMAGE);
        }

        this.mainImages.clear();

        IntStream.range(0, contexts.size())
                .mapToObj(i -> ProductMainImage.create(contexts.get(i), this, i+1))
                .forEach(this.mainImages::add);

        //TODO 커스텀 예외?
        ProductMainImage thumbnail = this.mainImages.stream().filter(ProductMainImage::isThumbnail).findFirst().orElseThrow();
        this.thumbnail = thumbnail.getImagePath();
    }

    public void addDetailImages(List<AddDetailImageContext> contexts) {
        if (this.status == ProductStatus.DELETED) {
            throw new BusinessException(ProductErrorCode.CANNOT_ADD_DETAIL_IMAGE);
        }

        this.detailImages.clear();

        IntStream.range(0, contexts.size())
                .mapToObj(i -> ProductDetailImage.create(contexts.get(i), this, i+1))
                .forEach(this.detailImages::add);
    }
}
