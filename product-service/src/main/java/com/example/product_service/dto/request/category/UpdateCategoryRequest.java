package com.example.product_service.dto.request.category;

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
    private String name;
    private Long parentId;
    @URL(message = "{invalid.url}")
    private String iconUrl;
}
