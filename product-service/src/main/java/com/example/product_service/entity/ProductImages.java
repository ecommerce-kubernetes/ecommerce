package com.example.product_service.entity;

import com.example.product_service.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductImages extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Products product;
    private String imageUrl;
    private Integer sortOrder;

    public ProductImages(Products product, String imageUrl, int sortOrder){
        this.product = product;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
        product.getImages().add(this);
    }
}
