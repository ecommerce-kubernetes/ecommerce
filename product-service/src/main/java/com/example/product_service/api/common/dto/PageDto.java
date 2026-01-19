package com.example.product_service.api.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
@Setter
public class PageDto<T> {
    private List<T> content;
    private int currentPage;
    private long totalPage;
    private int pageSize;
    private long totalElement;

    @Builder
    private PageDto(List<T> content, int currentPage, long totalPage, int pageSize, long totalElement) {
        this.content = content;
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        this.pageSize = pageSize;
        this.totalElement = totalElement;
    }

    public static <E, T> PageDto<T> of(Page<E> page, Function<E, T> mapper) {
        return PageDto.<T>builder()
                .content(page.map(mapper).getContent())
                .currentPage(page.getNumber() + 1)
                .totalPage(page.getTotalPages())
                .pageSize(page.getSize())
                .totalElement(page.getTotalElements())
                .build();
    }
}
