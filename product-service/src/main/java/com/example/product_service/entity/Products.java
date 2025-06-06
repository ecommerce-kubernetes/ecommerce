package com.example.product_service.entity;

import com.example.product_service.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Products extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Categories category;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImages> images = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOptionTypes> productOptionTypes = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariants> productVariants = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reviews> reviews = new ArrayList<>();

    public Products(String name, String description, Categories category){
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void addImage(String imageUrl, int sortOrder){
        ProductImages image = new ProductImages(this, imageUrl, sortOrder);
        images.add(image);
    }

    public void removeImage(ProductImages image) {
        images.remove(image);
        image.setProduct(null);
    }

    public void addProductOptionTypes(OptionTypes optionTypes, int priority, boolean active){
        ProductOptionTypes productOptionType =
                new ProductOptionTypes(this, optionTypes, priority, active);

        this.productOptionTypes.add(productOptionType);
    }

    public void addProductVariants(String sku, int price, int stockQuantity, int discountValue,
                                   List<OptionValues> optionValues){
        ProductVariants productVariant = new ProductVariants(this, sku, price, stockQuantity, discountValue);

        for (OptionValues optionValue : optionValues) {
            productVariant.addProductVariantOption(optionValue);
        }
        this.productVariants.add(productVariant);
    }

    public void addReviews(Long userId, int rating, String content, List<String> imageUrls){
        Reviews reviews = new Reviews(this, userId, rating, content);
        if(imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                reviews.addImage(imageUrl);
            }
        }
        this.reviews.add(reviews);
    }

    public void deleteImage(ProductImages productImage){
        images.remove(productImage);
        productImage.setProduct(null);
    }

    public void modifyBasicInfo(String name, String description, Categories category){
        if(name != null){
            this.name = name;
        }
        if(description != null){
            this.description = description;
        }
        if(category != null){
            this.category = category;
        }
    }

}
