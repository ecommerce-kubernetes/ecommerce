package com.example.image_service.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    String saveImage(MultipartFile file, String directory);
    void deleteImage(String imageUrl);
    void deleteImageBatch(List<String> imageUrls);
}
