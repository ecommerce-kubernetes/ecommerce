package com.example.product_service.api.product.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductSearchCondition {
    private static final int MAX_SIZE = 100;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;

    private Integer page = DEFAULT_PAGE;
    private Integer size = DEFAULT_SIZE;

    private String sort = "latest";

    @Min(value = 1, message = "카테고리 Id는 음수일 수 없습니다")
    private Long categoryId;
    private String name;
    @Min(value = 0, message = "평점은 음수일 수 없습니다")
    @Max(value = 5, message = "최대 평점은 5점입니다")
    private Integer rating;

    @Builder
    private ProductSearchCondition(Integer page, Integer size, String sort, Long categoryId, String name, Integer rating) {
        this.page = page;
        this.size = size;
        this.sort = sort;
        this.categoryId = categoryId;
        this.name = name;
        this.rating = rating;
    }
}
