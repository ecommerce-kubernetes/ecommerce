package com.example.product_service.entity;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.common.entity.BaseEntity;
import com.example.product_service.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    private String name;
    @Setter
    private String description;
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOptionType> productOptionTypes = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> productVariants = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Product(String name, String description, Category category){
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public static Product create(String name, String description, Category category) {
        return Product.builder()
                .name(name)
                .description(description)
                .category(category)
                .build();
    }

    public void addImages(List<ProductImage> images){
        for(ProductImage image : images){
            addImage(image);
        }
    }

    public void addImage(ProductImage image){
        image.setSortOrder(this.images.size());
        this.images.add(image);
        image.setProduct(this);
    }

    public void deleteImage(ProductImage image){
        boolean removed = this.images.remove(image);
        if(!removed){
            return;
        }

        image.setProduct(null);

        for(int i=0; i<this.images.size(); i++){
            ProductImage img = this.images.get(i);
            img.setSortOrder(i);
        }
    }

    public void swapImageSortOrder(ProductImage image, int sortOrder){
        ProductImage productImage = this.images.stream().filter(pi -> pi.getSortOrder() == sortOrder).findFirst()
                .orElseThrow(() -> new BadRequestException("sortOrder cannot be modified"));

        productImage.setSortOrder(image.getSortOrder());
        image.setSortOrder(sortOrder);
    }

    public void addOptionTypes(List<ProductOptionType> productOptionTypes){
        for (ProductOptionType productOptionType : productOptionTypes) {
            addOptionType(productOptionType);
        }
    }

    public void addOptionType(ProductOptionType productOptionType){
        this.productOptionTypes.add(productOptionType);
        productOptionType.setProduct(this);
    }

    public void addVariants(List<ProductVariant> productVariants){
        for (ProductVariant productVariant : productVariants) {
            addVariant(productVariant);
        }
    }

    public void addVariant(ProductVariant productVariant){
        Set<Long> variantOptionTypeIds = productVariant.getProductVariantOptions()
                .stream()
                .map(pvo -> pvo.getOptionValue().getOptionType().getId())
                .collect(Collectors.toSet());
        Set<Long> allowedOptionTypeId =
                this.productOptionTypes.stream().map(pot -> pot.getOptionType().getId()).collect(Collectors.toSet());

        if(!variantOptionTypeIds.equals(allowedOptionTypeId)){
            throw new BadRequestException("OptionValue must belong to the OptionType");
        }

        Set<Long> variantOptionValueIds = productVariant.getProductVariantOptions()
                .stream().map(pvo -> pvo.getOptionValue().getId()).collect(Collectors.toSet());

        boolean exists = productVariants.stream()
                .map(pv -> pv.getProductVariantOptions().stream()
                        .map(pvo -> pvo.getOptionValue().getId()).collect(Collectors.toSet()))
                .anyMatch(existing -> existing.equals(variantOptionValueIds));

        if(exists){
            throw new BadRequestException("Cannot add product variants with the same OptionValue");
        }
        this.productVariants.add(productVariant);
        productVariant.setProduct(this);
    }

    public void deleteVariant(ProductVariant productVariant){
        if(this.productVariants.size() == 1){
            throw new BadRequestException("must be at least one product variant per product");
        }
        this.productVariants.remove(productVariant);
        productVariant.setProduct(null);
    }
}
