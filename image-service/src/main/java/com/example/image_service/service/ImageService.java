package com.example.image_service.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String saveImage(MultipartFile file);
    void deleteImage(String imageUrl);
}
