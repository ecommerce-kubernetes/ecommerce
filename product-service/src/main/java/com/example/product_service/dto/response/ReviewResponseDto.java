package com.example.product_service.dto.response;

import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.entity.ProductOptionTypes;
import com.example.product_service.entity.Reviews;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private Long id;
    private String productName;
    private Long userId;
    private String userName;
    private int rating;
    private String content;
    private List<OptionValueResponse> optionValues;
    private LocalDateTime createdAt;
}
