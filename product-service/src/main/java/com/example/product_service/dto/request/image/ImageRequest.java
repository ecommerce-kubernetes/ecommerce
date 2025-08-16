package com.example.product_service.dto.request.image;

import com.example.product_service.dto.validation.AtLeastOneFieldNotNull;
import jakarta.validation.constraints.Min;
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
@AtLeastOneFieldNotNull
public class ImageRequest {
    @Pattern(regexp = "^(?!\\s*$).+", message = "{NotBlank}")
    @URL(message = "{InvalidUrl}")
    private String url;
}
