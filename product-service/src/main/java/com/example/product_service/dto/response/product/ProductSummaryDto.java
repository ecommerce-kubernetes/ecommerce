package com.example.product_service.dto.response.product;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

@Getter
@ToString
@Setter
@NoArgsConstructor
public class ProductSummaryDto {
    private Long id;
    private String name;
    private String description;
    private String thumbnailUrl;
    private int originPrice;
    private int discountPrice;
    private int discountValue;

    @QueryProjection
    public ProductSummaryDto(Long id, String name, String description,
                             String thumbnailUrl, int originPrice, int discountPrice, int discountValue){
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.originPrice = originPrice;
        this.discountPrice = discountPrice;
        this.discountValue = discountValue;
    }

    @QueryProjection
    public ProductSummaryDto(Long id, String name, String description, String thumbnailUrl, int originPrice){
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.originPrice = originPrice;
    }
}
