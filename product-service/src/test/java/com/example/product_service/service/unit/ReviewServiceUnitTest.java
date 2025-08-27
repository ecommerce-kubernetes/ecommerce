package com.example.product_service.service.unit;

import com.example.product_service.common.MessagePath;
import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.entity.*;
import com.example.product_service.exception.NoPermissionException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.ReviewsRepository;
import com.example.product_service.service.ReviewService;
import com.example.product_service.util.TestMessageUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceUnitTest {

    @Mock
    ReviewsRepository reviewsRepository;
    @Mock
    MessageSourceUtil ms;

    @InjectMocks
    ReviewService reviewService;

    @Test
    @DisplayName("리뷰 삭제 테스트-성공")
    void deleteReviewByIdTest_unit_success(){
        OptionType optionType = new OptionType("optionType");
        OptionValue optionValue = createOptionValue(1L, "optionValue", optionType);
        ProductOptionType productOptionType = createProductOptionTypes(optionType);

        ProductVariant productVariant = createProductVariant(1L, "sku", List.of(new ProductVariantOption(optionValue)));
        Review review1 = new Review(1L, "user", 4, "content");
        Review review2 = new Review(2L, "user", 3, "content");
        productVariant.addReview(review1);
        productVariant.addReview(review2);

        createProduct(List.of(new ProductImage("http://test.jpg")), List.of(productOptionType),
                List.of(productVariant));

        when(reviewsRepository.findWithVariantById(1L))
                .thenReturn(Optional.of(review1));

        reviewService.deleteReviewById(1L, 1L);

        assertThat(productVariant.getReviews().size()).isEqualTo(1);
        assertThat(productVariant.getReviews())
                .extracting(Review::getUserId, Review::getUserName, Review::getRating, Review::getContent)
                .containsExactlyInAnyOrder(
                        tuple(2L, "user", 3, "content")
                );
    }

    @Test
    @DisplayName("리뷰 삭제 테스트-실패(리뷰를 찾을 수 없음)")
    void deleteReviewByIdTest_unit_notFound_review(){
        when(reviewsRepository.findWithVariantById(1L))
                .thenReturn(Optional.empty());
        when(ms.getMessage(MessagePath.REVIEW_NOT_FOUND)).thenReturn("Review not found");

        assertThatThrownBy(() -> reviewService.deleteReviewById(1L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(TestMessageUtil.getMessage(MessagePath.REVIEW_NOT_FOUND));
    }

    @Test
    @DisplayName("리뷰 삭제 테스트-실패(리뷰 작성자가 아닌 경우)")
    void deleteReviewByIdTest_unit_NoPermission(){
        OptionType optionType = new OptionType("optionType");
        OptionValue optionValue = createOptionValue(1L, "optionValue", optionType);
        ProductOptionType productOptionType = createProductOptionTypes(optionType);

        ProductVariant productVariant = createProductVariant(1L, "sku", List.of(new ProductVariantOption(optionValue)));
        Review review1 = new Review(1L, "user", 4, "content");
        Review review2 = new Review(2L, "user", 3, "content");
        productVariant.addReview(review1);
        productVariant.addReview(review2);

        createProduct(List.of(new ProductImage("http://test.jpg")), List.of(productOptionType),
                List.of(productVariant));

        when(reviewsRepository.findWithVariantById(1L))
                .thenReturn(Optional.of(review1));

        when(ms.getMessage(MessagePath.REVIEW_FORBIDDEN_DELETE))
                .thenReturn("not have permission to delete");

        assertThatThrownBy(() -> reviewService.deleteReviewById(1L, 5L))
                .isInstanceOf(NoPermissionException.class)
                .hasMessage(TestMessageUtil.getMessage(MessagePath.REVIEW_FORBIDDEN_DELETE));
    }

    private Product createProduct(List<ProductImage> productImages, List<ProductOptionType> productOptionTypes,
                                  List<ProductVariant> productVariants){
        Product product = new Product("productName", "product description",
                new Category("category", "http://test.jpg"));

        product.addImages(productImages);
        product.addOptionTypes(productOptionTypes);
        product.addVariants(productVariants);
        return product;
    }

    private ProductVariant createProductVariant(Long variantId, String sku, List<ProductVariantOption> productVariantOptions){
        ProductVariant productVariant = new ProductVariant(sku, 10000, 100, 10);
        productVariant.addProductVariantOptions(productVariantOptions);
        ReflectionTestUtils.setField(productVariant, "id", variantId);
        return productVariant;
    }

    private ProductOptionType createProductOptionTypes(OptionType optionType){
        return new ProductOptionType(optionType, 0, true);
    }

    private OptionValue createOptionValue(Long id, String name, OptionType optionType){
        OptionValue optionValue = new OptionValue(name);
        ReflectionTestUtils.setField(optionValue, "id", id);
        optionType.addOptionValue(optionValue);
        return optionValue;
    }
}
