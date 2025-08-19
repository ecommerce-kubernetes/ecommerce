package com.example.product_service.dto.response.product;

import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.dto.response.options.ProductOptionTypeResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.ProductImages;
import com.example.product_service.entity.ProductOptionTypes;
import com.example.product_service.entity.ProductVariants;
import com.example.product_service.entity.Products;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ImageResponse> images;
    private List<ProductOptionTypeResponse> productOptionTypes;
    private List<ProductVariantResponse> productVariants;


    public ProductResponse(Products products){
        this.id = products.getId();
        this.name = products.getName();
        this.description = products.getDescription();
        this.categoryId = products.getCategory().getId();
        this.createdAt = products.getCreateAt();
        this.updatedAt = products.getUpdateAt();
        this.images = products.getImages().stream()
                .map(i -> new ImageResponse(i.getId(), i.getImageUrl(), i.getSortOrder())).toList();
        this.productOptionTypes = products.getProductOptionTypes().stream()
                .map(pr -> new ProductOptionTypeResponse(pr.getOptionType().getId(), pr.getOptionType().getName())).toList();
        this.productVariants =
                products.getProductVariants().stream()
                        .map(pv ->
                                new ProductVariantResponse(pv.getId(), pv.getSku(), pv.getPrice(), pv.getStockQuantity(),
                                        pv.getDiscountValue(), pv.getProductVariantOptions().stream()
                                        .map(ot -> new OptionValueResponse(ot.getOptionValue())).toList())).toList();
    }

    public ProductResponse(Products product, List<ProductImages> productImages,
                           List<ProductOptionTypes> productOptionTypes, List<ProductVariants> productVariants){

        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.categoryId = product.getCategory().getId();
        this.createdAt = product.getCreateAt();

        this.images = productImages.stream().map(ImageResponse::new).toList();
        this.productOptionTypes = productOptionTypes.stream()
                .map(pot -> new ProductOptionTypeResponse(pot.getOptionType())).toList();

        this.productVariants = productVariants.stream().map(ProductVariantResponse::new)
                .toList();
    }
}
