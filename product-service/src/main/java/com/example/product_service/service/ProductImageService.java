package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.image.UpdateImageRequest;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.entity.ProductImage;
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

    public ImageResponse updateImageById(Long imageId, UpdateImageRequest request) {
        ProductImage productImage = findWithProductByIdOrThrow(imageId);

        if(request.getUrl() != null && !request.getUrl().isEmpty()){
            productImage.setImageUrl(request.getUrl());
        }

        if(request.getSortOrder() != null){
            productImage.getProduct().swapImageSortOrder(productImage, request.getSortOrder());
        }

        return new ImageResponse(productImage);
    }

    public void deleteImageById(Long imageId) {
        ProductImage productImage = findWithProductByIdOrThrow(imageId);
        productImage.getProduct().deleteImage(productImage);
    }

    private ProductImage findWithProductByIdOrThrow(Long imageId){
        return productImagesRepository.findWithProductById(imageId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(PRODUCT_IMAGE_NOT_FOUND)));
    }
}
