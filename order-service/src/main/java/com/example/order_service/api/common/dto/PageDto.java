package com.example.order_service.api.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PageDto<T> {
    private List<T> content;
    private int currentPage;
    private long totalPage;
    private int pageSize;
    private long totalElement;

    @Builder
    private PageDto(List<T> content, int currentPage, long totalPage, int pageSize, long totalElement){
        this.content = content;
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        this.pageSize = pageSize;
        this.totalElement = totalElement;
    }

    public static <T> PageDto<T> of(List<T> content, int currentPage, long totalPage, int pageSize, long totalElement){
        return PageDto.<T>builder()
                .content(content)
                .currentPage(currentPage + 1)
                .totalPage(totalPage)
                .pageSize(pageSize)
                .totalElement(totalElement)
                .build();
    }

}
