package com.example.product_service.product.adapter.in.web.dto.request;

import com.example.product_service.product.application.service.dto.command.ProductCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import org.springframework.data.domain.PageRequest;

@Builder
public record SearchProductRequest(
        Integer page,
        Integer size,
        String sort,
        @Min(value = 1, message = "카테고리 Id는 0 또는 음수일 수 없습니다")
        Long categoryId,
        String name,
        @Min(value = 0, message = "평점은 음수일 수 없습니다")
        @Max(value = 5, message = "최대 평점은 5점입니다")
        Integer rating
) {

    public SearchProductRequest {
        if (page == null) page = 1;
        if (size == null) size = 20;
        if (sort == null || sort.isBlank()) sort = "latest";
    }

    public ProductCommand.Search toCommand() {
        int validPage = (this.page > 0) ? this.page - 1 : 0;
        int validSize = (this.size > 0) ? Math.min(this.size, 100) : 20;

        return ProductCommand.Search.builder()
                .categoryId(categoryId)
                .name(name)
                .rating(rating)
                .pageable(PageRequest.of(validPage, validSize))
                .sort(sort)
                .build();
    }
}
