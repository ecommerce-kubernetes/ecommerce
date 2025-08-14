package com.example.product_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PageDto<T> {
    private List<T> content;
    private int currentPage;
    private long totalPage;
    private int pageSize;
    private long totalElement;
}
