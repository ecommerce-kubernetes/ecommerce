package com.example.product_service.entity;

import com.example.product_service.entity.base.BaseEntity;
import com.example.product_service.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.HashSet;
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
        this.images.add(image);
        image.setProduct(this);
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

        this.productVariants.add(productVariant);
        productVariant.setProduct(this);
    }

}
