package com.example.product_service.dto.request.review;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    @NotNull(message = "{NotNull}")
    private Long orderId;
    @NotNull(message = "{NotNull}")
    @Min(value = 1, message = "{Min}")
    @Max(value = 5, message = "{Max}")
    private Integer rating;
    @NotBlank(message = "{NotBlank}")
    private String content;
    @Valid
    private List<@NotBlank(message = "{NotBlank}") @URL(message = "{InvalidUrl}") String> imageUrls = new ArrayList<>();
}
