package com.example.product_service.service.unit;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.image.UpdateImageRequest;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.ProductImages;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.ProductImagesRepository;
import com.example.product_service.service.ProductImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Optional;

import static com.example.product_service.common.MessagePath.PRODUCT_IMAGE_NOT_FOUND;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductImageServiceUnitTest {

    @Mock
    ProductImagesRepository productImagesRepository;
    @Mock
    MessageSourceUtil ms;

    @InjectMocks
    ProductImageService productImageService;

    @Test
    @DisplayName("이미지 수정 테스트-성공")
    void updateImageByIdTest_unit_success(){
        ProductImages productImage = createProductImage(1L, "http://productImage1.jpg", 0);
        ProductImages productImage1 = createProductImage(2L, "http://productImage2.jpg", 1);
        ProductImages productImage2 = createProductImage(3L, "http://productImage3.jpg", 2);
        when(productImagesRepository.findWithProductById(2L))
                .thenReturn(Optional.of(productImage1));
        Products product = createProduct(productImage, productImage1, productImage2);

        ImageResponse response =
                productImageService.updateImageById(2L, new UpdateImageRequest("http://updateImage.jpg", 0));


        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getUrl()).isEqualTo("http://updateImage.jpg");
        assertThat(response.getSortOrder()).isEqualTo(0);

        assertThat(product.getImages().size()).isEqualTo(3);
        assertThat(product.getImages())
                .extracting(ProductImages::getId, ProductImages::getImageUrl, ProductImages::getSortOrder)
                .containsExactlyInAnyOrder(
                        tuple(1L, "http://productImage1.jpg", 1),
                        tuple(2L, "http://updateImage.jpg", 0),
                        tuple(3L, "http://productImage3.jpg", 2)
                );
    }

    @Test
    @DisplayName("이미지 수정 테스트-실패(이미지를 찾을 수 없음)")
    void updateImageByIdTest_unit_notFound_ProductImage(){
        UpdateImageRequest request = new UpdateImageRequest("http://update.jpg", 2);

        when(productImagesRepository.findWithProductById(1L))
                .thenReturn(Optional.empty());
        when(ms.getMessage(PRODUCT_IMAGE_NOT_FOUND))
                .thenReturn("ProductImage not found");
        assertThatThrownBy(() -> productImageService.updateImageById(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_IMAGE_NOT_FOUND));
    }

    @Test
    @DisplayName("이미지 수정 테스트-실패(잘못된 정렬 순서 요청)")
    void updateImageByIdTest_unit_badRequest_sortOrder(){
        ProductImages productImage = createProductImage(1L, "http://productImage1.jpg", 0);
        ProductImages productImage1 = createProductImage(2L, "http://productImage2.jpg", 1);
        ProductImages productImage2 = createProductImage(3L, "http://productImage3.jpg", 2);
        when(productImagesRepository.findWithProductById(2L))
                .thenReturn(Optional.of(productImage1));
        Products product = createProduct(productImage, productImage1, productImage2);


        assertThatThrownBy(() -> productImageService
                .updateImageById(2L,new UpdateImageRequest("http://updateImage.jpg", 5)))
                        .isInstanceOf(BadRequestException.class)
                        .hasMessage("sortOrder cannot be modified");
    }

    @Test
    @DisplayName("이미지 삭제 테스트-성공")
    void deleteImageByIdTest_unit_success(){
        ProductImages productImage = createProductImage(1L, "http://productImage1.jpg", 0);
        ProductImages productImage1 = createProductImage(2L, "http://productImage2.jpg", 1);
        ProductImages productImage2 = createProductImage(3L, "http://productImage3.jpg", 2);
        when(productImagesRepository.findWithProductById(2L))
                .thenReturn(Optional.of(productImage1));
        Products product = createProduct(productImage, productImage1, productImage2);

        productImageService.deleteImageById(2L);

        assertThat(product.getImages().size()).isEqualTo(2);
        assertThat(product.getImages())
                .extracting("id", "imageUrl", "sortOrder")
                .containsExactlyInAnyOrder(
                        tuple(1L, "http://productImage1.jpg", 0),
                        tuple(3L, "http://productImage3.jpg", 1)
                );
    }

    @Test
    @DisplayName("이미지 삭제 테스트-실패(이미지를 찾을 수 없음)")
    void deleteImageByIdTest_unit_notFound_image(){
        when(productImagesRepository.findWithProductById(1L))
                .thenReturn(Optional.empty());
        when(ms.getMessage(PRODUCT_IMAGE_NOT_FOUND))
                .thenReturn("ProductImage not found");
        assertThatThrownBy(() -> productImageService.deleteImageById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_IMAGE_NOT_FOUND));
    }

    private Products createProduct(ProductImages... productImages){
        Products product = new Products("productName", "product description",
                new Categories("category", "http://test.jpg"));

        product.addImages(Arrays.stream(productImages).toList());
        return product;
    }

    private ProductImages createProductImage(Long id, String url, int sortOrder){
        ProductImages productImage = new ProductImages(url);
        ReflectionTestUtils.setField(productImage, "id", id);
        ReflectionTestUtils.setField(productImage, "sortOrder", sortOrder);

        return productImage;
    }
}
