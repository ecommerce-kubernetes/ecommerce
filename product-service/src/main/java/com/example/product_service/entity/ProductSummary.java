package com.example.product_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "vw_product_summary")
@AllArgsConstructor
@NoArgsConstructor
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
    private int discountedPrice;
    private int discountRate;
    private LocalDateTime createAt;
}
