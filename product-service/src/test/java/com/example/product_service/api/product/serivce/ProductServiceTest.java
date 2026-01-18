package com.example.product_service.api.product.serivce;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.category.domain.repository.CategoryRepository;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CategoryErrorCode;
import com.example.product_service.api.common.exception.OptionErrorCode;
import com.example.product_service.api.common.exception.ProductErrorCode;
import com.example.product_service.api.option.domain.model.OptionType;
import com.example.product_service.api.option.domain.model.OptionValue;
import com.example.product_service.api.option.domain.repository.OptionTypeRepository;
import com.example.product_service.api.product.domain.model.Product;
import com.example.product_service.api.product.domain.model.ProductVariant;
import com.example.product_service.api.product.domain.repository.ProductRepository;
import com.example.product_service.api.product.service.ProductService;
import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import com.example.product_service.api.product.service.dto.command.ProductVariantsCreateCommand;
import com.example.product_service.api.product.service.dto.result.*;
import com.example.product_service.support.ExcludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
public class ProductServiceTest extends ExcludeInfraTest {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private OptionTypeRepository optionTypeRepository;

    private ProductCreateCommand createProductCommand(Long categoryId){
        return ProductCreateCommand.builder()
                .name("상품")
                .categoryId(categoryId)
                .description("상품 설명")
                .build();
    }

    private ProductVariantsCreateCommand.ProductVariantsCreateCommandBuilder createVariantCommand() {
        return ProductVariantsCreateCommand.builder()
                .productId(1L)
                .variants(
                        List.of(ProductVariantsCreateCommand.VariantDetail.builder()
                                .originalPrice(3000L)
                                .discountRate(10)
                                .stockQuantity(100)
                                .optionValueIds(List.of(1L, 2L)).build())
                );
    }

    private Category saveCategory() {
        return categoryRepository.save(Category.create("카테고리", null, "http://image.jpg"));
    }

    private Product saveProduct(Category category) {
        return productRepository.save(Product.create("상품", "설명", category));
    }

    private OptionType saveOptionType(String name, List<String> valueNames) {
        return optionTypeRepository.save(OptionType.create(name, valueNames));
    }

    private OptionValue findOptionValue(OptionType optionType, String name) {
        return optionType.getOptionValues().stream().filter(v -> v.getName().equals(name)).findFirst().orElseThrow();
    }

    @Nested
    @DisplayName("상품 생성")
    class Create {

        @Test
        @DisplayName("상품을 생성한다")
        void createProduct(){
            //given
            Category category = saveCategory();
            ProductCreateCommand command = createProductCommand(category.getId());
            //when
            ProductCreateResponse result = productService.createProduct(command);
            //then
            assertThat(result.getProductId()).isNotNull();
        }

        @Test
        @DisplayName("카테고리를 찾을 수 없으면 예외를 던진다")
        void createProduct_category_notFound(){
            //given
            ProductCreateCommand command = createProductCommand(999L);
            //when
            //then
            assertThatThrownBy(() -> productService.createProduct(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("상품 옵션 설정")
    class OptionSpecs {

        @Test
        @DisplayName("상품 옵션을 설정한다")
        void defineOptions(){
            //given
            OptionType size = saveOptionType("사이즈", List.of("XL", "L", "M", "S"));
            OptionType color = saveOptionType("색상", List.of("RED", "BLUE"));
            Category category = saveCategory();
            Product product = saveProduct(category);
            //when
            ProductOptionResponse response = productService.defineOptions(product.getId(), List.of(size.getId(), color.getId()));
            //then
            assertThat(response.getProductId()).isEqualTo(product.getId());
            assertThat(response.getOptions())
                    .extracting(ProductOptionResponse.OptionDto::getOptionTypeId, ProductOptionResponse.OptionDto::getOptionTypeName,
                            ProductOptionResponse.OptionDto::getPriority)
                    .containsExactly(
                            tuple(size.getId(), size.getName(), 1),
                            tuple(color.getId(), color.getName(), 2)
                    );
        }

        @Test
        @DisplayName("상품 옵션을 설정할때 상품을 찾을 수 없으면 예외를 던진다")
        void defineOptions_notFound_product(){
            //given
            //when
            //then
            assertThatThrownBy(() -> productService.defineOptions(999L, List.of(1L,2L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("옵션 타입을 찾을 수 없는 경우 예외를 던진다")
        void defineOptions_not_found_optionType(){
            //given
            OptionType size = saveOptionType("사이즈", List.of("XL", "L", "M", "S"));
            Category category = saveCategory();
            Product product = saveProduct(category);
            //when
            //then
            assertThatThrownBy(() -> productService.defineOptions(product.getId(), List.of(size.getId(), 999L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OptionErrorCode.OPTION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("상품 변형 추가")
    class Variant {

        @Test
        @DisplayName("상품 변형을 생성한다")
        void createVariant(){
            //given
            Category category = saveCategory();
            Product product = Product.create("상품", "상품 설명", category);
            OptionType size = saveOptionType("사이즈", List.of("XL", "L", "M", "S"));
            OptionType color = saveOptionType("색상", List.of("RED", "BLUE"));
            OptionValue xl = findOptionValue(size, "XL");
            OptionValue blue = findOptionValue(color, "BLUE");
            OptionValue red = findOptionValue(color, "RED");
            ProductVariant variant = ProductVariant.create("TEST", 10000L, 100, 10);
            variant.addProductVariantOptions(List.of(xl, red));
            product.updateOptions(List.of(size, color));
            product.addVariant(variant);
            productRepository.save(product);
            ProductVariantsCreateCommand command = createVariantCommand()
                    .productId(product.getId()).variants(
                            List.of(
                                    ProductVariantsCreateCommand.VariantDetail.builder()
                                            .originalPrice(3000L)
                                            .discountRate(10)
                                            .stockQuantity(100)
                                            .optionValueIds(List.of(xl.getId(), blue.getId())).build()))
                    .build();
            //when
            VariantCreateResponse result = productService.createVariants(command);
            //then
            String createdSku = "PROD" + product.getId() + "-" +xl.getName() + "-" + blue.getName();
            assertThat(result.getProductId()).isEqualTo(product.getId());
            assertThat(result.getVariants())
                    .allSatisfy(v -> assertThat(v.getVariantId()).isNotNull());

            assertThat(result.getVariants())
                    .hasSize(1)
                    .satisfiesExactly(
                            variantResp -> {
                                assertThat(variantResp.getVariantId()).isNotNull();
                                assertThat(variantResp.getSku()).isEqualTo(createdSku);
                                assertThat(variantResp.getOriginalPrice()).isEqualTo(3000L);
                                assertThat(variantResp.getDiscountRate()).isEqualTo(10);
                                assertThat(variantResp.getOptionValueIds())
                                        .containsExactlyInAnyOrder(xl.getId(), blue.getId());
                            }
                    );
        }

        @Test
        @DisplayName("동일한 옵션 조합의 상품 변형을 여러개 생성할 수 없다 [상품 옵션이 있는 경우]")
        void createVariants_duplicate_variant_request_has_option_case(){
            //given
            ProductVariantsCreateCommand command = createVariantCommand().variants(
                    List.of(
                            ProductVariantsCreateCommand.VariantDetail.builder()
                                    .originalPrice(3000L)
                                    .discountRate(10)
                                    .stockQuantity(100)
                                    .optionValueIds(List.of(1L, 2L)).build(),
                            ProductVariantsCreateCommand.VariantDetail.builder()
                                    .originalPrice(3000L)
                                    .discountRate(10)
                                    .stockQuantity(100)
                                    .optionValueIds(List.of(1L, 2L)).build()))
                    .build();
            //when
            //then
            assertThatThrownBy(() -> productService.createVariants(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.DUPLICATE_VARIANT_IN_REQUEST);
        }

        @Test
        @DisplayName("동일한 옵션 조합의 상품 변형을 여러개 생성할 수 없다 [상품 옵션이 없는 경우]")
        void createVariants_duplicate_variant_request_no_option_case(){
            //given
            ProductVariantsCreateCommand command = createVariantCommand().variants(
                            List.of(
                                    ProductVariantsCreateCommand.VariantDetail.builder()
                                            .originalPrice(3000L)
                                            .discountRate(10)
                                            .stockQuantity(100)
                                            .optionValueIds(List.of()).build(),
                                    ProductVariantsCreateCommand.VariantDetail.builder()
                                            .originalPrice(3000L)
                                            .discountRate(10)
                                            .stockQuantity(100)
                                            .optionValueIds(List.of()).build()))
                    .build();
            //when
            //then
            assertThatThrownBy(() -> productService.createVariants(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.DUPLICATE_VARIANT_IN_REQUEST);        }

        @Test
        @DisplayName("상품을 찾을 수 없으면 예외를 던진다")
        void createVariants_not_found_product(){
            //given
            ProductVariantsCreateCommand command = createVariantCommand().productId(999L).build();
            //when
            //then
            assertThatThrownBy(() -> productService.createVariants(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("상품 옵션을 찾을 수 없으면 예외를 던진다")
        void createVariants_notFound_optionValue(){
            //given
            Category category = saveCategory();
            Product product = saveProduct(category);
            ProductVariantsCreateCommand command = createVariantCommand()
                    .productId(product.getId()).variants(
                            List.of(
                                    ProductVariantsCreateCommand.VariantDetail.builder()
                                            .originalPrice(3000L)
                                            .discountRate(10)
                                            .stockQuantity(100)
                                            .optionValueIds(List.of(999L)).build()))
                    .build();
            //when
            //then
            assertThatThrownBy(() -> productService.createVariants(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OptionErrorCode.OPTION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("상품 이미지 추가")
    class AddImage {
        @Test
        @DisplayName("상품을 찾을 수 없으면 예외를 던진다")
        void addImages_notFound_product(){
            //given
            //when
            //then
            assertThatThrownBy(() -> productService.addImages(999L, List.of("http://image1.jpg", "http://image2.jpg")))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("상품 이미지를 추가한다")
        void addImages(){
            //given
            Category category = Category.create("카테고리", null, "http://image.jpg");
            categoryRepository.save(category);
            Product product = Product.create("상품", "상품 설명", category);
            productRepository.save(product);
            //when
            ProductImageCreateResponse result = productService.addImages(product.getId(), List.of("http://prod1.jpg", "http://prod2.jpg"));
            //then
            assertThat(result.getProductId()).isEqualTo(product.getId());
            assertThat(result.getImages())
                    .allSatisfy(image ->
                            assertThat(image.getProductImageId()).isNotNull()
                    );
            assertThat(result.getImages())
                    .extracting(ProductImageResponse::getImageUrl, ProductImageResponse::getOrder, ProductImageResponse::isThumbnail)
                    .containsExactlyInAnyOrder(
                            tuple("http://prod1.jpg", 1, true),
                            tuple("http://prod2.jpg", 2, false)
                    );
        }
    }

    @Nested
    @DisplayName("상품 게시")
    class Publish {
        @Test
        @DisplayName("상품을 찾을 수 없는 경우 예외를 던진다")
        void publish_notFound_product(){
            //given
            //when
            //then
            assertThatThrownBy(() -> productService.publish(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("상품을 게시한다")
        void publish(){
            //given
            Category category = Category.create("카테고리", null, "http://image.jpg");
            categoryRepository.save(category);
            Product product = Product.create("상품", "상품 설명", category);
            ProductVariant variant = ProductVariant.create("TEST", 3000L, 100, 10);
            product.addVariant(variant);
            product.addImages(List.of("http://image.jpg"));
            productRepository.save(product);
            //when
            ProductStatusResponse result = productService.publish(product.getId());
            //then
            assertThat(result.getProductId()).isEqualTo(product.getId());
            assertThat(result.getStatus()).isEqualTo("ON_SALE");
            assertThat(result.getChangedAt()).isNotNull();
        }
    }
}
