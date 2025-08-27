package com.example.product_service.dto.response.product;

import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.dto.response.options.ProductOptionTypeResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.Product;
import com.example.product_service.entity.ProductImage;
import com.example.product_service.entity.ProductOptionType;
import com.example.product_service.entity.ProductVariant;
import com.example.product_service.service.dto.ReviewStats;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private Long reviewCount;
    private Double avgRating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ImageResponse> images;
    private List<ProductOptionTypeResponse> productOptionTypes;
    private List<ProductVariantResponse> productVariants;


    public ProductResponse(Product product){
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.categoryId = product.getCategory().getId();
        this.createdAt = product.getCreateAt();
        this.updatedAt = product.getUpdateAt();
        this.images = product.getImages().stream()
                .map(i -> new ImageResponse(i.getId(), i.getImageUrl(), i.getSortOrder())).toList();
        this.productOptionTypes = product.getProductOptionTypes().stream()
                .map(pr -> new ProductOptionTypeResponse(pr.getOptionType().getId(), pr.getOptionType().getName())).toList();
        this.productVariants =
                product.getProductVariants().stream()
                        .map(pv ->
                                new ProductVariantResponse(pv.getId(), pv.getSku(), pv.getPrice(), pv.getStockQuantity(),
                                        pv.getDiscountValue(), pv.getProductVariantOptions().stream()
                                        .map(ot -> new OptionValueResponse(ot.getOptionValue())).toList())).toList();
    }

    public ProductResponse(Product product, List<ProductImage> productImages,
                           List<ProductOptionType> productOptionTypes, List<ProductVariant> productVariants,
                           ReviewStats reviewStats){

        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.categoryId = product.getCategory().getId();
        this.reviewCount = reviewStats.getReviewCount();
        this.avgRating = reviewStats.getAvgRating();

        this.createdAt = product.getCreateAt();

        this.images = productImages.stream().map(ImageResponse::new).toList();
        this.productOptionTypes = productOptionTypes.stream()
                .map(pot -> new ProductOptionTypeResponse(pot.getOptionType())).toList();

        this.productVariants = productVariants.stream().map(ProductVariantResponse::new)
                .toList();
    }
}
