package com.example.product_service.dto.request;

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
public class ReviewRequestDto {
    @NotNull(message = "orderId is required")
    private Long orderId;
    @NotNull(message = "rating is required")
    private int rating;
    @NotBlank(message = "content is Not Blank")
    private String content;
    private List<@NotBlank @URL(message = "Invalid ImgUrl") String> imgUrls = new ArrayList<>();
}
