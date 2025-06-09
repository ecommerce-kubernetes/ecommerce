package com.example.product_service.service;

import com.example.product_service.dto.request.ImageOrderRequestDto;
import com.example.product_service.dto.request.ProductImageRequestDto;
import com.example.product_service.dto.response.product.ProductResponseDto;
import com.example.product_service.entity.ProductImages;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.ProductImagesRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.client.ImageClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService{

    private final ProductImagesRepository productImagesRepository;
    private final ProductsRepository productsRepository;
    private final ImageClientService imageClientService;

    @Override
    public ProductResponseDto addImage(Long productId, ProductImageRequestDto requestDto) {
        List<String> imageUrls = requestDto.getImageUrls();
        Products product = productsRepository.findByIdWithProductImages(productId)
                .orElseThrow(() -> new NotFoundException("Not Found Product"));

        int nextOrder = product.getImages().stream()
                .map(ProductImages::getSortOrder)
                .max(Integer::compareTo)
                .orElse(-1) + 1;

        for (String imageUrl : imageUrls) {
            product.addImage(imageUrl, nextOrder++);
        }
        return new ProductResponseDto(product);
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        ProductImages productImage =
                productImagesRepository.findById(imageId)
                        .orElseThrow(() -> new NotFoundException("Not Found ProductImage"));

        Integer deletedSortOrder = productImage.getSortOrder();
        imageClientService.deleteImage(productImage.getImageUrl());
        Products product = productImage.getProduct();
        product.removeImage(productImage);
        for(ProductImages img : product.getImages()){
            if(img.getSortOrder() > deletedSortOrder){
                img.setSortOrder(img.getSortOrder() -1);
            }
        }
    }

    @Override
    @Transactional
    public ProductResponseDto imgSwapOrder(Long imageId, ImageOrderRequestDto requestDto) {
        ProductImages target = productImagesRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Not Found ProductImages"));
        Long productId = target.getProduct().getId();

        int newOrder = requestDto.getSortOrder();
        int totalImages = productImagesRepository.countByProductId(productId);
        if(newOrder >= totalImages || newOrder < 0){
            throw new BadRequestException("sortOrder for the image cannot exceed the size of the image list");
        }

        ProductImages conflict = productImagesRepository.findByProductIdAndSortOrder(productId, newOrder)
                .orElseThrow(() -> new NotFoundException("Not Found ProductImage"));

        int oldOrder = target.getSortOrder();

        target.setSortOrder(newOrder);
        conflict.setSortOrder(oldOrder);

        Products product = productsRepository.findByIdWithImageAndCategory(productId)
                .orElseThrow(() -> new NotFoundException("Not Found Product"));

        return new ProductResponseDto(product);
    }
}
