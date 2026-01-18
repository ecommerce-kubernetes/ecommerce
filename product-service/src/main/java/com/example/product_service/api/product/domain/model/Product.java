package com.example.product_service.api.product.domain.model;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.common.entity.BaseEntity;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.ProductErrorCode;
import com.example.product_service.api.option.domain.model.OptionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public void addVariant(ProductVariant productVariant) {
        validateAddableVariant(productVariant);
        this.variants.add(productVariant);
        productVariant.setProduct(this);
        updatePrice();
    }

    public void updateOptions(List<OptionType> newOptionTypes) {
        validateUpdatableOptions(newOptionTypes);
        replaceOptions(newOptionTypes);
    }

    public void replaceImages(List<String> imageUrls) {
        if (this.status == ProductStatus.DELETED) {
            throw new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        if (this.status == ProductStatus.ON_SALE) {
            if (imageUrls == null || imageUrls.isEmpty()) {
                throw new BusinessException(ProductErrorCode.CANNOT_DELETE_ALL_IMAGES_ON_SALE);
            }
        }

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

    private void validateAddableVariant(ProductVariant productVariant) {
        if (this.status == ProductStatus.DELETED) {
            throw new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        if (productVariant.getProductVariantOptions().size() != this.options.size()) {
            throw new BusinessException(ProductErrorCode.NOT_MATCH_PRODUCT_OPTION_SIZE);
        }
        validateVariantOptionSpec(productVariant);
        validateHasDuplicateVariant(productVariant);
    }

    private void validateVariantOptionSpec(ProductVariant productVariant) {
        Set<Long> productOptionTypeIds = this.options.stream().map(po -> po.getOptionType().getId()).collect(Collectors.toSet());
        for (ProductVariantOption variantOption : productVariant.getProductVariantOptions()) {
            Long variantOptionTypeId = variantOption.getOptionValue().getOptionType().getId();
            if (!productOptionTypeIds.contains(variantOptionTypeId)){
                throw new BusinessException(ProductErrorCode.NOT_MATCH_PRODUCT_OPTION_SPEC);
            }
        }
    }

    private void validateHasDuplicateVariant(ProductVariant productVariant) {
        Set<Long> variantOptions = productVariant.getProductVariantOptions().stream()
                .map(pvo -> pvo.getOptionValue().getId()).collect(Collectors.toSet());
        for (ProductVariant variant : this.variants) {
            boolean isDuplicate = variant.hasSameOptions(variantOptions);
            if (isDuplicate) {
                throw new BusinessException(ProductErrorCode.PRODUCT_HAS_DUPLICATE_VARIANT);
            }
        }
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

        if (this.originalPrice == null || this.originalPrice <= 0) {
            throw new BusinessException(ProductErrorCode.INVALID_ORIGINAL_PRICE);
        }

        if (this.lowestPrice > this.originalPrice) {
            throw new BusinessException(ProductErrorCode.DISPLAY_PRICE_GREATER_THAN_ORIGINAL);
        }

        if (this.maxDiscountRate == null || this.maxDiscountRate < 0) {
            throw new BusinessException(ProductErrorCode.INVALID_DISCOUNT_RATE);
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
