package com.example.product_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "vw_product_summary")
@AllArgsConstructor
@Getter
public class ProductSummary {
    @Id
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String thumbnail;
    private double avgRating;
    private int reviewCount;
    private int minimumPrice;
    private int discountPrice;
    private int discountRate;
    private LocalDateTime createdAt;
}
