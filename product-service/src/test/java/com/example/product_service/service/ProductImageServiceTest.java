package com.example.product_service.service;

import com.example.product_service.common.MessagePath;
import com.example.product_service.dto.request.image.UpdateImageRequest;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.entity.*;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.OptionTypeRepository;
import com.example.product_service.repository.ProductImagesRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.util.TestMessageUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductImageServiceTest {

    @Autowired
    ProductsRepository productsRepository;
    @Autowired
    OptionTypeRepository optionTypeRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductImagesRepository productImagesRepository;
    @Autowired
    EntityManager em;

    @Autowired
    ProductImageService productImageService;

    OptionType storage;
    Category electronic;
    OptionValue gb_128;

    @BeforeEach
    void saveFixture(){
        storage = new OptionType("용량");
        electronic = new Category("전자 기기", "http://electronic.jpg");
        gb_128 = new OptionValue("128GB");
        storage.addOptionValue(gb_128);
        optionTypeRepository.save(storage);
        categoryRepository.save(electronic);
    }

    @Test
    @DisplayName("상품 이미지 수정 테스트-성공")
    void updateImageById_integration_success(){
        ProductImage productImage1 = createProductImages("http://iphone16-1.jpg", 0);
        ProductImage productImage2 = createProductImages("http://iphone16-2.jpg", 1);
        ProductImage productImage3 = createProductImages("http://iphone16-3.jpg", 2);
        ProductOptionType productOptionType = createProductOptionType(storage);
        ProductVariant productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Product product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage1, productImage2, productImage3), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);
        em.flush(); em.clear();

        ImageResponse response = productImageService.updateImageById(productImage2.getId(),
                new UpdateImageRequest("http://update.jpg", 0));

        assertThat(response.getId()).isEqualTo(productImage2.getId());
        assertThat(response.getUrl()).isEqualTo("http://update.jpg");
        assertThat(response.getSortOrder()).isEqualTo(0);

        Product findProduct = productsRepository.findById(product.getId()).get();

        assertThat(findProduct.getImages())
                .extracting(ProductImage::getId, ProductImage::getImageUrl, ProductImage::getSortOrder)
                .containsExactlyInAnyOrder(
                        tuple(productImage1.getId(), productImage1.getImageUrl(), 1),
                        tuple(productImage2.getId(), "http://update.jpg", 0),
                        tuple(productImage3.getId(), productImage3.getImageUrl(), 2)
                );
    }

    @Test
    @DisplayName("상품 이미지 수정 테스트-실패(상품 이미지를 찾을 수 없음)")
    void updateImageById_integration_notFound_productImage(){
        assertThatThrownBy(() -> productImageService
                .updateImageById(999L, new UpdateImageRequest("http://updateImage.jpg", 0)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(TestMessageUtil.getMessage(MessagePath.PRODUCT_IMAGE_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 이미지 수정 테스트-실패(잘못된 정렬 순서 요청)")
    void updateImageById_integration_badRequest_sortOrder(){
        ProductImage productImage1 = createProductImages("http://iphone16-1.jpg", 0);
        ProductImage productImage2 = createProductImages("http://iphone16-2.jpg", 1);
        ProductImage productImage3 = createProductImages("http://iphone16-3.jpg", 2);
        ProductOptionType productOptionType = createProductOptionType(storage);
        ProductVariant productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Product product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage1, productImage2, productImage3), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);
        em.flush(); em.clear();

        assertThatThrownBy(() -> productImageService.
                updateImageById(productImage2.getId(), new UpdateImageRequest("http://updateImage.jpg", 5)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("sortOrder cannot be modified");
    }

    @Test
    @DisplayName("상품 이미지 삭제 테스트-성공")
    void deleteImageById_integration_success(){
        ProductImage productImage1 = createProductImages("http://iphone16-1.jpg", 0);
        ProductImage productImage2 = createProductImages("http://iphone16-2.jpg", 1);
        ProductImage productImage3 = createProductImages("http://iphone16-3.jpg", 2);
        ProductOptionType productOptionType = createProductOptionType(storage);
        ProductVariant productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Product product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage1, productImage2, productImage3), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);

        productImageService.deleteImageById(productImage2.getId());
        em.flush(); em.clear();

        Product findProduct = productsRepository.findById(product.getId()).get();

        assertThat(findProduct.getImages().size()).isEqualTo(2);
        assertThat(findProduct.getImages())
                .extracting(ProductImage::getId, ProductImage::getImageUrl, ProductImage::getSortOrder)
                .containsExactlyInAnyOrder(
                        tuple(productImage1.getId(), productImage1.getImageUrl(), 0),
                        tuple(productImage3.getId(), productImage3.getImageUrl(), 1)
                );
    }

    @Test
    @DisplayName("상품 이미지 삭제 테스트-실패(이미지를 찾을 수 없음)")
    void deleteImageById_integration_notFound_product(){
        assertThatThrownBy(() -> productImageService.deleteImageById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(TestMessageUtil.getMessage(MessagePath.PRODUCT_IMAGE_NOT_FOUND));
    }

    private ProductImage createProductImages(String imageUrl, int sortOrder){
        return new ProductImage(imageUrl, sortOrder);
    }

    private ProductOptionType createProductOptionType(OptionType optionType){
        return new ProductOptionType(optionType, 0, true);
    }

    private ProductVariant createProductVariants(String sku, int price, int stockQuantity, int discountValue, OptionValue optionValue){
        ProductVariantOption productVariantOption = new ProductVariantOption(optionValue);

        ProductVariant productVariant = new ProductVariant(sku, price, stockQuantity, discountValue);
        productVariant.addProductVariantOption(productVariantOption);
        return productVariant;
    }

    private Product createProduct(String name, String description, Category category,
                                  List<ProductImage> productImages, List<ProductOptionType> productOptionTypes,
                                  List<ProductVariant> productVariants){
        Product product = new Product(name, description, category);
        product.addImages(productImages);
        product.addOptionTypes(productOptionTypes);
        product.addVariants(productVariants);

        return product;
    }
}