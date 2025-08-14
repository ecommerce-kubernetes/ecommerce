package com.example.product_service.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageUrlListDto {
    @NotEmpty(message = "At least one image URL is required")
    private List<@NotBlank @URL(message = "Invalid image URL") String> imageUrls;
}
