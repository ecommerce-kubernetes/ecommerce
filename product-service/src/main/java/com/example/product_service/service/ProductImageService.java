package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.entity.ProductImages;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.ProductImagesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.product_service.common.MessagePath.PRODUCT_IMAGE_NOT_FOUND;


@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageService {
    private final ProductImagesRepository productImagesRepository;
    private final MessageSourceUtil ms;

    public ImageResponse updateImageById(Long imageId, ImageRequest request) {
        return null;
    }

    public void deleteImageById(Long imageId) {
        ProductImages productImage = findWithProductByIdOrThrow(imageId);
        productImage.getProduct().deleteImage(productImage);
    }

    private ProductImages findWithProductByIdOrThrow(Long imageId){
        return productImagesRepository.findWithProductById(imageId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(PRODUCT_IMAGE_NOT_FOUND)));
    }
}
