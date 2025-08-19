package com.example.product_service.dto.response.image;

import com.example.product_service.entity.ProductImages;
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

    public ImageResponse(ProductImages image){
        this.id = image.getId();
        this.url = image.getImageUrl();
        this.sortOrder = image.getSortOrder();
    }
}
