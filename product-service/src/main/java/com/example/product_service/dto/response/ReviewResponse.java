package com.example.product_service.dto.response;

import com.example.product_service.dto.response.options.OptionValueResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private String productName;
    private Long userId;
    private String userName;
    private int rating;
    private String content;
    private List<OptionValueResponse> optionValues;
    private LocalDateTime createdAt;
}
