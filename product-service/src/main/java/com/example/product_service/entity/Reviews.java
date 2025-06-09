package com.example.product_service.entity;

import com.example.product_service.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Reviews extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariants productVariant;

    private Long userId;
    private int rating;
    private String content;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ReviewImages> images = new ArrayList<>();

    public Reviews(ProductVariants productVariant, Long userId, int rating, String content){
        this.productVariant = productVariant;
        this.userId = userId;
        this.rating = rating;
        this.content = content;
    }

    public void addImage(String url){

    }
}
