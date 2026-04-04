package com.example.product_service.api.product.controller.dto.request;

import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

public class ProductRequest {

    @Builder
    public record CreateRequest(
            @NotBlank(message = "상품 이름은 필수 입니다")
            String name,
            @NotNull(message = "카테고리 id는 필수 입니다")
            Long categoryId,
            String description
    )
    {
        public ProductCreateCommand toCommand() {
            return ProductCreateCommand.builder()
                    .name(this.name())
                    .categoryId(this.categoryId())
                    .description(this.description())
                    .build();
        }
    }

    @Builder
    public record OptionRegisterRequest(
            @NotNull(message = "옵션 id 리스트는 필수 입니다")
            @Size(max = 3, message = "옵션은 최대 3개까지만 설정 가능합니다")
            @UniqueElements(message = "중복된 옵션 종류가 포함되어 있습니다")
            List<Long> optionTypeIds
    ) {}
}
