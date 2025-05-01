package com.example.product_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "image-service")
public interface ImageClient {

    @DeleteMapping("/images/delete")
    public ResponseEntity<Void> deleteImage(@RequestParam("imageUrl") String imageUrl);
}
