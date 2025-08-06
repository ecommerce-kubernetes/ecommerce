package com.example.product_service.dto.request.image;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageRequest {
    @NotBlank(message = "{image.url.notBlank}")
    @URL(message = "{invalid.url}")
    private String url;
    @NotNull(message = "{image.sortOrder.notNull}")
    @Min(value = 0, message = "{image.sortOrder.min}")
    private Integer sortOrder;
}
