package com.example.product_service.dto.response;

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
    private List<String> optionValues;
    private LocalDateTime createAt;

    public ReviewResponseDto(Reviews reviews){
        this.id = reviews.getId();
        this.productName = reviews.getProductVariant().getProduct().getName();
        this.userId = reviews.getUserId();
        this.userName = reviews.getUserName();
        this.rating = reviews.getRating();
        this.content = reviews.getContent();

        Map<Long, Integer> priorityMap = reviews.getProductVariant()
                .getProduct()
                .getProductOptionTypes().stream()
                .collect(Collectors.toMap(
                        pot -> pot.getOptionType().getId(),
                        ProductOptionTypes::getPriority
                ));
        this.optionValues = reviews.getProductVariant().getProductVariantOptions().stream()
                .sorted(Comparator.comparingInt(pvo ->
                        priorityMap.getOrDefault(
                                pvo.getOptionValue().getOptionType().getId(),
                                Integer.MAX_VALUE
                        )
                ))
                .map(pvo -> pvo.getOptionValue().getOptionValue())
                .toList();

        this.createAt = reviews.getCreateAt();
    }
}
