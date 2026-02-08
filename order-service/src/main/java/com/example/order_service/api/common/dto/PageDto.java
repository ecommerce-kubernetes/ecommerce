package com.example.order_service.api.common.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
@Builder
public class PageDto<T> {
    private List<T> content;
    private int currentPage;
    private long totalPage;
    private int pageSize;
    private long totalElement;

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
