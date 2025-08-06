package com.example.product_service.dto.request.image;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddImageRequest {
    @NotNull(message = "{NotNull}")
    @NotEmpty(message = "{NotEmpty}")
    private List<@URL(message = "{InvalidUrl}") String> imageUrls;
}
