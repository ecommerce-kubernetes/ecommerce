package com.example.product_service.entity;

import com.example.product_service.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductImages extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Products product;
    private String imageUrl;
    @Setter
    private Integer sortOrder;

    public ProductImages(Products product, String imageUrl, int sortOrder){
        this.product = product;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    protected void setProduct(Products product){
        this.product = product;
    }

}
