package com.example.image_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageURLDto {
    private String imageUrl;
    public ImageURLDto(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
