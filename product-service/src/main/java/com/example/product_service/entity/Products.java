package com.example.product_service.entity;

import com.example.product_service.entity.base.BaseEntity;
import com.example.product_service.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Products extends BaseEntity {

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
    private Categories category;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImages> images = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOptionTypes> productOptionTypes = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariants> productVariants = new ArrayList<>();

    public Products(String name, String description, Categories category){
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void addImages(List<ProductImages> images){
        for(ProductImages image : images){
            addImage(image);
        }
    }

    public void addImage(ProductImages image){
        image.setSortOrder(this.images.size());
        this.images.add(image);
        image.setProduct(this);
    }

    public void deleteImage(ProductImages image){
        boolean removed = this.images.remove(image);
        if(!removed){
            return;
        }

        image.setProduct(null);

        for(int i=0; i<this.images.size(); i++){
            ProductImages img = this.images.get(i);
            img.setSortOrder(i);
        }
    }

    public void swapImageSortOrder(ProductImages image, int sortOrder){
        ProductImages productImage = this.images.stream().filter(pi -> pi.getSortOrder() == sortOrder).findFirst()
                .orElseThrow(() -> new BadRequestException("sortOrder cannot be modified"));

        productImage.setSortOrder(image.getSortOrder());
        image.setSortOrder(sortOrder);
    }

    public void addOptionTypes(List<ProductOptionTypes> productOptionTypes){
        for (ProductOptionTypes productOptionType : productOptionTypes) {
            addOptionType(productOptionType);
        }
    }

    public void addOptionType(ProductOptionTypes productOptionType){
        this.productOptionTypes.add(productOptionType);
        productOptionType.setProduct(this);
    }

    public void addVariants(List<ProductVariants> productVariants){
        for (ProductVariants productVariant : productVariants) {
            addVariant(productVariant);
        }
    }

    public void addVariant(ProductVariants productVariant){
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
}
