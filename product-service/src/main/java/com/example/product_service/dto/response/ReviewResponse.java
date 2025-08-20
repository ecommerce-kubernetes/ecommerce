package com.example.product_service.dto.response;

import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.entity.Reviews;
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

    public ReviewResponse(Reviews review){
        this.id = review.getId();
        this.productName = review.getProductVariant().getProduct().getName();
        this.userId = review.getUserId();
        this.userName = review.getUserName();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.optionValues = review.getProductVariant().getProductVariantOptions()
                .stream().map(pvo -> new OptionValueResponse(pvo.getOptionValue())).toList();
        this.createdAt = review.getCreateAt();
    }
}
