package com.example.product_service.dto.request.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "카테고리 생성 요청 Request")
public class CategoryRequest {

    @Schema(description = "카테고리 이름", example = "의류")
    @NotBlank(message = "{category.name.notBlank}")
    private String name;

    @Schema(description = "부모 카테고리 ID", example = "3")
    private Long parentId;

    @Schema(description = "아이콘 URL", example = "http://image.jpg")
    @URL(message = "{category.iconUrl.invalid}")
    private String iconUrl;
}
