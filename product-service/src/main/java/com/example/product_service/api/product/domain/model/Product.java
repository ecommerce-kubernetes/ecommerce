package com.example.product_service.api.product.domain.model;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.common.entity.BaseEntity;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.ProductErrorCode;
import com.example.product_service.api.option.domain.model.OptionType;
import com.example.product_service.api.option.domain.model.OptionValue;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product extends BaseEntity {

    public static final int MAX_PRODUCT_OPTION_COUNT = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    @Enumerated(EnumType.STRING)
    private ProductStatus status;
    private String description;
    private LocalDateTime publishedAt;

    private String thumbnail;
    private Double rating;
    private Long reviewCount;
    private Double popularityScore;

    private Long lowestPrice;
    private Long originalPrice;
    private Integer maxDiscountRate;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Product(String name, Category category, ProductStatus status, String description, LocalDateTime publishedAt, String thumbnail, Double rating, Long reviewCount, Double popularityScore, Long lowestPrice, Long originalPrice, Integer maxDiscountRate) {
        this.name = name;
        this.category = category;
        this.status = status;
        this.description = description;
        this.publishedAt = publishedAt;
        this.thumbnail = thumbnail;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.popularityScore = popularityScore;
        this.lowestPrice = lowestPrice;
        this.originalPrice = originalPrice;
        this.maxDiscountRate = maxDiscountRate;
    }

    public static Product create(String name, String description, Category category) {
        if (category == null) {
            throw new BusinessException(ProductErrorCode.PRODUCT_CATEGORY_REQUIRED);
        }
        if (!category.isLeaf()) {
            throw new BusinessException(ProductErrorCode.CATEGORY_NOT_LEAF);
        }

        return Product.builder()
                .name(name)
                .category(category)
                .status(ProductStatus.PREPARING)
                .description(description)
                .rating(0.0)
                .reviewCount(0L)
                .popularityScore(0.0)
                .build();
    }

    public void deleted() {
        this.status = ProductStatus.DELETED;
    }

    public void publish() {
        validatePublishable();
        this.status = ProductStatus.ON_SALE;
        this.publishedAt = LocalDateTime.now();
    }

    private void validatePublishable() {
        if (this.status == ProductStatus.DELETED) {
            throw new BusinessException(ProductErrorCode.CANNOT_PUBLISH_DELETED_PRODUCT);
        }

        if (this.variants == null || this.variants.isEmpty()) {
            throw new BusinessException(ProductErrorCode.NO_VARIANTS_TO_PUBLISH);
        }

        if (this.thumbnail == null || this.thumbnail.isBlank()) {
            throw new BusinessException(ProductErrorCode.NO_THUMBNAIL_IMAGE);
        }

        if (this.lowestPrice == null || this.lowestPrice <= 0) {
            throw new BusinessException(ProductErrorCode.INVALID_DISPLAY_PRICE);
        }
    }

    public void validateCreatableVariantStatus() {
        if (this.status == ProductStatus.DELETED) {
            throw new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    public List<OptionValue> validateAndSortOptionValues(List<OptionValue> inputValues) {
        if (inputValues.size() != this.options.size()) {
            throw new BusinessException(ProductErrorCode.NOT_MATCH_PRODUCT_OPTION_SPEC);
        }
        List<OptionValue> sortedValues = new ArrayList<>();
        for (ProductOption spec : options) {
            OptionValue matchedValue = inputValues.stream()
                    .filter(val -> val.getOptionType().getId().equals(spec.getOptionType().getId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ProductErrorCode.NOT_MATCH_PRODUCT_OPTION_SPEC));
            sortedValues.add(matchedValue);
        }
        return sortedValues;
    }

    private void validateDuplicateVariant(List<OptionValue> optionValues) {
        Set<Long> targetIds = optionValues.stream()
                .map(OptionValue::getId)
                .collect(Collectors.toSet());
        for (ProductVariant variant : variants) {
            if (variant.hasSameOptions(targetIds)) {
                throw new BusinessException(ProductErrorCode.PRODUCT_HAS_DUPLICATE_VARIANT);
            }
        }
    }

    public void addVariant(ProductVariant productVariant) {
        validateDuplicateVariant(productVariant.getProductVariantOptions().stream().map(ProductVariantOption::getOptionValue).toList());
        this.variants.add(productVariant);
        productVariant.setProduct(this);
        updatePrice();
    }

    public void updateOptions(List<OptionType> newOptionTypes) {
        validateUpdatableOptions(newOptionTypes);
        replaceOptions(newOptionTypes);
    }

    public void addImages(List<String> imageUrls) {
        images.clear();
        for(int i=0; i<imageUrls.size(); i++) {
            this.images.add(ProductImage.create(this, imageUrls.get(i), i + 1));
        }

        updateThumbnail();
    }

    private void updatePrice() {
        this.variants.stream().min(Comparator.comparingLong(ProductVariant::getPrice))
                .ifPresent(cheapestVariant -> {
                    this.lowestPrice = cheapestVariant.getPrice();
                    this.originalPrice = cheapestVariant.getOriginalPrice();
                });
        this.variants.stream().max(Comparator.comparingInt(ProductVariant::getDiscountRate))
                .ifPresent(p -> this.maxDiscountRate = p.getDiscountRate());
    }

    private void updateThumbnail() {
        this.thumbnail = this.images.stream()
                .filter(image -> image.getSortOrder() == 1)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }

    private void validateUpdatableOptions(List<OptionType> optionTypes){
        // 판매중인 상품은 상품 옵션을 설정할 수 없음
        if (this.status == ProductStatus.ON_SALE) {
            throw new BusinessException(ProductErrorCode.CANNOT_MODIFY_PRODUCT_OPTION_ON_SALE);
        }

        // 상품에 상품 변형이 존재하면 상품 옵션을 설정할 수 없음
        if (!this.variants.isEmpty()) {
            throw new BusinessException(ProductErrorCode.CANNOT_MODIFY_PRODUCT_OPTION_HAS_VARIANTS);
        }

        // 상품에 설정할 수 있는 옵션의 최대 개수는 3개
        if (optionTypes.size() > MAX_PRODUCT_OPTION_COUNT) {
            throw new BusinessException(ProductErrorCode.EXCEED_PRODUCT_OPTION_COUNT);
        }

        // 상품에 동일한 옵션을 설정할 수 없음
        long uniqueCount = optionTypes.stream().distinct().count();
        if (uniqueCount != optionTypes.size()) {
            throw new BusinessException(ProductErrorCode.DUPLICATE_OPTION_TYPE);
        }
    }

    private void replaceOptions(List<OptionType> newOptionTypes) {
        this.options.clear();
        for (int i = 0; i < newOptionTypes.size(); i++) {
            this.options.add(
                    ProductOption.create(this, newOptionTypes.get(i), i+1)
            );
        }
    }
}
