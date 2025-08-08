package com.example.product_service.dto.request.category;

import com.example.product_service.dto.validation.AtLeastOneFieldNotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@AtLeastOneFieldNotNull(message = "{EmptyRequest}")
public class UpdateCategoryRequest {
    @Pattern(regexp = "^(?!\\s*$).+", message = "{NotBlank}")
    private String name;
    private Long parentId;
    @URL(message = "{InvalidUrl}")
    private String iconUrl;
}
