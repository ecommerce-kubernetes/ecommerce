package com.example.product_service.api.product.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
@NoArgsConstructor
public class ProductSearchCondition {
    private static final int MAX_SIZE = 100;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;

    private Integer page = DEFAULT_PAGE;
    private Integer size = DEFAULT_SIZE;

    private String sort = "latest";

    @Min(value = 1, message = "카테고리 Id는 0 또는 음수일 수 없습니다")
    @Setter
    private Long categoryId;
    @Setter
    private String name;
    @Min(value = 0, message = "평점은 음수일 수 없습니다")
    @Max(value = 5, message = "최대 평점은 5점입니다")
    @Setter
    private Integer rating;

    @Builder
    private ProductSearchCondition(Integer page, Integer size, String sort, Long categoryId, String name, Integer rating) {
        setPage(page);
        setSize(size);
        setSort(sort);
        this.categoryId = categoryId;
        this.name = name;
        this.rating = rating;
    }

    public void setPage(Integer page) {
        this.page = (page == null || page <= 0) ? DEFAULT_PAGE : page;
    }

    public void setSort(String sort) {
        if (sort != null && !sort.isBlank()) {
            this.sort = sort;
        }
    }

    public void setSize(Integer size) {
        if (size == null) {
            this.size = DEFAULT_SIZE;
        } else {
            this.size = Math.min(size, MAX_SIZE);
        }
    }

    public Pageable getPageable() {
        return PageRequest.of(this.page - 1, this.size);
    }
}
