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

    OptionTypes storage;
    Categories electronic;
    OptionValues gb_128;

    @BeforeEach
    void saveFixture(){
        storage = new OptionTypes("용량");
        electronic = new Categories("전자 기기", "http://electronic.jpg");
        gb_128 = new OptionValues("128GB");
        storage.addOptionValue(gb_128);
        optionTypeRepository.save(storage);
        categoryRepository.save(electronic);
    }

    @Test
    @DisplayName("상품 이미지 수정 테스트-성공")
    void updateImageById_integration_success(){
        ProductImages productImage1 = createProductImages("http://iphone16-1.jpg", 0);
        ProductImages productImage2 = createProductImages("http://iphone16-2.jpg", 1);
        ProductImages productImage3 = createProductImages("http://iphone16-3.jpg", 2);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Products product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage1, productImage2, productImage3), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);
        em.flush(); em.clear();

        ImageResponse response = productImageService.updateImageById(productImage2.getId(),
                new UpdateImageRequest("http://update.jpg", 0));

        assertThat(response.getId()).isEqualTo(productImage2.getId());
        assertThat(response.getUrl()).isEqualTo("http://update.jpg");
        assertThat(response.getSortOrder()).isEqualTo(0);

        Products findProduct = productsRepository.findById(product.getId()).get();

        assertThat(findProduct.getImages())
                .extracting(ProductImages::getId, ProductImages::getImageUrl, ProductImages::getSortOrder)
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
        ProductImages productImage1 = createProductImages("http://iphone16-1.jpg", 0);
        ProductImages productImage2 = createProductImages("http://iphone16-2.jpg", 1);
        ProductImages productImage3 = createProductImages("http://iphone16-3.jpg", 2);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Products product = createProduct("IPhone 16", "IPhone Model 16", electronic,
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
        ProductImages productImage1 = createProductImages("http://iphone16-1.jpg", 0);
        ProductImages productImage2 = createProductImages("http://iphone16-2.jpg", 1);
        ProductImages productImage3 = createProductImages("http://iphone16-3.jpg", 2);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Products product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage1, productImage2, productImage3), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);

        productImageService.deleteImageById(productImage2.getId());
        em.flush(); em.clear();

        Products findProduct = productsRepository.findById(product.getId()).get();

        assertThat(findProduct.getImages().size()).isEqualTo(2);
        assertThat(findProduct.getImages())
                .extracting(ProductImages::getId, ProductImages::getImageUrl, ProductImages::getSortOrder)
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

    private ProductImages createProductImages(String imageUrl, int sortOrder){
        return new ProductImages(imageUrl, sortOrder);
    }

    private ProductOptionTypes createProductOptionType(OptionTypes optionTypes){
        return new ProductOptionTypes(optionTypes, 0, true);
    }

    private ProductVariants createProductVariants(String sku, int price, int stockQuantity, int discountValue, OptionValues optionValues){
        ProductVariantOptions productVariantOptions = new ProductVariantOptions(optionValues);

        ProductVariants productVariants = new ProductVariants(sku, price, stockQuantity, discountValue);
        productVariants.addProductVariantOption(productVariantOptions);
        return productVariants;
    }

    private Products createProduct(String name, String description, Categories category,
                                   List<ProductImages> productImages, List<ProductOptionTypes> productOptionTypes,
                                   List<ProductVariants> productVariants){
        Products product = new Products(name, description, category);
        product.addImages(productImages);
        product.addOptionTypes(productOptionTypes);
        product.addVariants(productVariants);

        return product;
    }
}