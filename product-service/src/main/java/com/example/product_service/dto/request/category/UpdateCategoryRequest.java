package com.example.product_service.dto.request.category;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCategoryRequest {
    @Size(min = 1, message = "{NotBlank}")
    private String name;
    private Long parentId;
    @URL(message = "{InvalidUrl}")
    private String iconUrl;
}
