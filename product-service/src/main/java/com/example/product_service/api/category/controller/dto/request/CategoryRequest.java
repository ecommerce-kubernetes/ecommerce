package com.example.product_service.api.category.controller.dto.request;

import com.example.product_service.api.category.service.dto.command.CategoryCommand.Create;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

public class CategoryRequest {

    @Builder
    public record CreateRequest(
            @NotBlank(message = "name은 필수값입니다")
            String name,
            Long parentId,
            @Pattern(
                    regexp = "^/[\\w\\-/]+\\.(jpg|jpeg|png|gif|webp|JPG|JPEG|PNG|GIF|WEBP)$",
                    message = "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다"
            )
            String imagePath
    ) {
        public Create toCommand() {
            return Create.builder()
                    .name(name)
                    .parentId(parentId)
                    .imagePath(imagePath)
                    .build();
        }
    }

    @Builder
    public record UpdateRequest(
            String name,
            @Pattern(
                    regexp = "^/[\\w\\-/]+\\.(jpg|jpeg|png|gif|webp|JPG|JPEG|PNG|GIF|WEBP)$",
                    message = "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다"
            )
            String imagePath
    ) {
        @JsonIgnore
        @AssertTrue(message = "수정할 값이 하나는 존재해야합니다")
        public boolean isAtLeastOneFieldPresent() {
            return name != null || imagePath != null;
        }
    }

    @Builder
    public record MoveRequest(
            Long parentId
    ) { }
}
