package com.example.image_service.controller;

import com.example.image_service.controller.dto.request.PresignedRequest;
import com.example.image_service.service.ImageService;
import com.example.image_service.service.dto.result.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(@RequestBody @Validated PresignedRequest request) {
        PresignedUrlResponse response = imageService
                .generatePresignedUrl(request.getDomain(), request.getOriginalFilename());
        return ResponseEntity.ok(response);
    }
}
