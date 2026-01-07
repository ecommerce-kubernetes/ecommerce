package com.example.product_service.entity;

import com.example.product_service.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Setter
    private String imageUrl;
    private Integer sortOrder;

    public ProductImage(String imageUrl, int sortOrder){
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public ProductImage(String imageUrl){
        this.imageUrl = imageUrl;
    }

    protected void setSortOrder(int sortOrder){
        this.sortOrder = sortOrder;
    }

    protected void setProduct(Product product){
        this.product = product;
    }

}
