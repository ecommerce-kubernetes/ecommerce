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
        OptionTypes optionType = new OptionTypes("optionType");
        OptionValues optionValue = createOptionValue(1L, "optionValue", optionType);
        ProductOptionTypes productOptionType = createProductOptionTypes(optionType);

        ProductVariants productVariant = createProductVariant(1L, "sku", List.of(new ProductVariantOptions(optionValue)));
        Reviews review1 = new Reviews(1L, "user", 4, "content");
        Reviews review2 = new Reviews(2L, "user", 3, "content");
        productVariant.addReview(review1);
        productVariant.addReview(review2);

        createProduct(List.of(new ProductImages("http://test.jpg")), List.of(productOptionType),
                List.of(productVariant));

        when(reviewsRepository.findWithVariantById(1L))
                .thenReturn(Optional.of(review1));

        reviewService.deleteReviewById(1L, 1L);

        assertThat(productVariant.getReviews().size()).isEqualTo(1);
        assertThat(productVariant.getReviews())
                .extracting(Reviews::getUserId, Reviews::getUserName, Reviews::getRating, Reviews::getContent)
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
        OptionTypes optionType = new OptionTypes("optionType");
        OptionValues optionValue = createOptionValue(1L, "optionValue", optionType);
        ProductOptionTypes productOptionType = createProductOptionTypes(optionType);

        ProductVariants productVariant = createProductVariant(1L, "sku", List.of(new ProductVariantOptions(optionValue)));
        Reviews review1 = new Reviews(1L, "user", 4, "content");
        Reviews review2 = new Reviews(2L, "user", 3, "content");
        productVariant.addReview(review1);
        productVariant.addReview(review2);

        createProduct(List.of(new ProductImages("http://test.jpg")), List.of(productOptionType),
                List.of(productVariant));

        when(reviewsRepository.findWithVariantById(1L))
                .thenReturn(Optional.of(review1));

        when(ms.getMessage(MessagePath.REVIEW_FORBIDDEN_DELETE))
                .thenReturn("not have permission to delete");

        assertThatThrownBy(() -> reviewService.deleteReviewById(1L, 5L))
                .isInstanceOf(NoPermissionException.class)
                .hasMessage(TestMessageUtil.getMessage(MessagePath.REVIEW_FORBIDDEN_DELETE));
    }

    private Products createProduct(List<ProductImages> productImages, List<ProductOptionTypes> productOptionTypes,
                                   List<ProductVariants> productVariants){
        Products product = new Products("productName", "product description",
                new Categories("category", "http://test.jpg"));

        product.addImages(productImages);
        product.addOptionTypes(productOptionTypes);
        product.addVariants(productVariants);
        return product;
    }

    private ProductVariants createProductVariant(Long variantId, String sku, List<ProductVariantOptions> productVariantOptions){
        ProductVariants productVariant = new ProductVariants(sku, 10000, 100, 10);
        productVariant.addProductVariantOptions(productVariantOptions);
        ReflectionTestUtils.setField(productVariant, "id", variantId);
        return productVariant;
    }

    private ProductOptionTypes createProductOptionTypes(OptionTypes optionTypes){
        return new ProductOptionTypes(optionTypes, 0, true);
    }

    private OptionValues createOptionValue(Long id, String name, OptionTypes optionTypes){
        OptionValues optionValue = new OptionValues(name);
        ReflectionTestUtils.setField(optionValue, "id", id);
        optionTypes.addOptionValue(optionValue);
        return optionValue;
    }
}
