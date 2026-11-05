package com.example.product_service.support;

import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.category.exception.CategoryErrorCode;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@TestComponent
@RestController
public class ExceptionTestController {

    @GetMapping("/exception/business")
    public String throwBusinessException() {
        throw new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND);
    }

    @PostMapping("/exception/validation")
    public String throwValidationException(@Validated @RequestBody ValidationTestRequest request) {
        return request.name();
    }

    @Builder
    public record ValidationTestRequest(
            @NotBlank(message = "이름은 필수입니다")
            String name
    ) { }
}
