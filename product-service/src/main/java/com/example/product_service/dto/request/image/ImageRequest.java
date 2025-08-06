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
    @NotBlank(message = "{NotBlank}")
    @URL(message = "{InvalidUrl}")
    private String url;
    @NotNull(message = "{NotNull}")
    @Min(value = 0, message = "{Min}")
    private Integer sortOrder;
}
