package com.example.product_service.dto.response.image;

import com.example.product_service.api.product.domain.model.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {
    private Long id;
    private String url;
    private int sortOrder;

    public ImageResponse(ProductImage image){
        this.id = image.getId();
        this.url = image.getImageUrl();
        this.sortOrder = image.getSortOrder();
    }
}
