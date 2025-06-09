package com.example.product_service.dto.response;

import com.example.product_service.entity.Reviews;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private Long id;
    private Long productId;
    private Long userId;
    private int rating;
    private String content;

    public ReviewResponseDto(Reviews reviews){
        this.id = reviews.getId();
        this.userId = reviews.getUserId();
        this.rating = reviews.getRating();
        this.content = reviews.getContent();
    }
}
