package com.example.product_service.service;

import com.example.product_service.entity.*;
import com.example.product_service.exception.NoPermissionException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.product_service.common.MessagePath.REVIEW_FORBIDDEN_DELETE;
import static com.example.product_service.common.MessagePath.REVIEW_NOT_FOUND;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class ReviewServiceTest {

    @Autowired
    ProductsRepository productsRepository;
    @Autowired
    OptionTypeRepository optionTypeRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductVariantsRepository productVariantsRepository;
    @Autowired
    EntityManager em;
    @Autowired
    ReviewService reviewService;

    private Product apple;

    OptionType storage;
    Category electronic;
    OptionValue gb_128;
    OptionValue gb_256;

    @BeforeEach
    void saveFixture(){
        storage = new OptionType("용량");
        electronic = new Category("전자 기기", "http://electronic.jpg");
        gb_128 = new OptionValue("128GB");
        gb_256 = new OptionValue("256GB");
        storage.addOptionValue(gb_128);
        storage.addOptionValue(gb_256);
        optionTypeRepository.save(storage);
        categoryRepository.save(electronic);
    }


    @Test
    @DisplayName("상품 리뷰 삭제 테스트-성공")
    void deleteReviewByIdTest_integration_success(){
        ProductImage productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionType productOptionType = createProductOptionType(storage);
        ProductVariant productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Review review1 = new Review(1L, "user1", 4, "good");
        Review review2 = new Review(2L, "user2", 5, "very good");
        productVariant.addReview(review1);
        productVariant.addReview(review2);

        Product product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);
        reviewService.deleteReviewById(review1.getId(), 1L);
        em.flush(); em.clear();

        ProductVariant findVariant = productVariantsRepository.findById(productVariant.getId()).get();
        assertThat(findVariant.getReviews().size()).isEqualTo(1);
        assertThat(findVariant.getReviews())
                .extracting(Review::getId, Review::getUserId, Review::getUserName, Review::getRating,
                        Review::getContent)
                .containsExactlyInAnyOrder(
                        tuple(review2.getId(), review2.getUserId(), review2.getUserName(), review2.getRating(),
                                review2.getContent())
                );
    }

    @Test
    @DisplayName("상품 리뷰 삭제 테스트-실패(상품 리뷰를 찾을 수 없음)")
    void deleteReviewByIdTest_integration_notFound_Review(){
        ProductImage productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionType productOptionType = createProductOptionType(storage);
        ProductVariant productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Product product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);
        em.flush(); em.clear();

        assertThatThrownBy(() -> reviewService.deleteReviewById(999L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(REVIEW_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 리뷰 삭제 테스트-실패(작성자가 아닌경우)")
    void deleteReviewByIdTest_integration_NoPermission(){
        ProductImage productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionType productOptionType = createProductOptionType(storage);
        ProductVariant productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Review review1 = new Review(1L, "user1", 4, "good");
        Review review2 = new Review(2L, "user2", 5, "very good");
        productVariant.addReview(review1);
        productVariant.addReview(review2);

        Product product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);
        em.flush(); em.clear();

        assertThatThrownBy(() -> reviewService.deleteReviewById(review1.getId(), 999L))
                .isInstanceOf(NoPermissionException.class)
                .hasMessage(getMessage(REVIEW_FORBIDDEN_DELETE));
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
