package com.example.order_service.api.order.controller.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class OrderSearchCondition {
    private static final int MAX_SIZE = 100;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;

    private Integer page = DEFAULT_PAGE;
    private Integer size = DEFAULT_SIZE;
    private String sort = "latest";
    @Setter
    private String year;
    @Setter
    private String productName;

    @Builder
    private OrderSearchCondition(Integer page, Integer size, String sort, String year, String productName) {
        setPage(page);
        setSize(size);
        setSort(sort);
        this.year = year;
        this.productName = productName;
    }

    public void setPage(Integer page) {
        this.page = (page == null || page <= 0) ? DEFAULT_PAGE : page;
    }

    public void setSize(Integer size) {
        if (size == null) {
            this.size = DEFAULT_SIZE;
        } else {
            this.size = Math.min(size, MAX_SIZE);
        }
    }

    public void setSort(String sort) {
        if (sort != null && !sort.isBlank()){
            this.sort = sort;
        }
    }
}
