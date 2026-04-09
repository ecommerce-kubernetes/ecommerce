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
import com.example.product_service.api.product.domain.model.ProductStatus;
import com.example.product_service.api.product.domain.model.ProductVariant;
import com.example.product_service.api.product.domain.repository.ProductRepository;
import com.example.product_service.api.product.service.ProductService;
import com.example.product_service.api.product.service.dto.command.ProductCommand;
import com.example.product_service.api.product.service.dto.result.ProductResult;
import com.example.product_service.support.ExcludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private ProductCommand.Create createProductCommand(Long categoryId){
        return ProductCommand.Create.builder()
                .name("상품")
                .categoryId(categoryId)
                .description("상품 설명")
                .build();
    }

    private ProductCommand.AddVariant.AddVariantBuilder createVariantCommand() {
        return ProductCommand.AddVariant.builder()
                .productId(1L)
                .variants(
                        List.of(ProductCommand.VariantDetail.builder()
                                .originalPrice(3000L)
                                .discountRate(10)
                                .stockQuantity(100)
                                .optionValueIds(List.of(1L, 2L)).build())
                );
    }

    private Category saveCategory() {
        return categoryRepository.save(Category.create("카테고리", null, "/test/image.jpg"));
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

    private Product settingProduct(Category category, String name) {
        Product product = Product.create(name, "상품 설명", category);
        ProductVariant variant = ProductVariant.create("TEST", 10000L, 100, 10);
        product.addVariant(variant);
        product.replaceImages(List.of("/test/image.jpg"));
        product.replaceDescriptionImage(List.of("/test/description.jpg"));
        product.publish();
        return productRepository.save(product);
    }

    @Nested
    @DisplayName("상품 생성")
    class Create {

        @Test
        @DisplayName("상품을 생성한다")
        void createProduct(){
            //given
            Category category = saveCategory();
            ProductCommand.Create command = createProductCommand(category.getId());
            //when
            ProductResult.Create result = productService.createProduct(command);
            //then
            assertThat(result.productId()).isNotNull();
        }

        @Test
        @DisplayName("카테고리를 찾을 수 없으면 예외를 던진다")
        void createProduct_category_notFound(){
            //given
            ProductCommand.Create command = createProductCommand(999L);
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
            ProductCommand.OptionRegister command = ProductCommand.OptionRegister.builder()
                    .productId(product.getId())
                    .optionTypeIds(List.of(size.getId(), color.getId()))
                    .build();
            //when
            ProductResult.OptionRegister result = productService.defineOptions(command);
            //then
            assertThat(result.productId()).isEqualTo(product.getId());
            assertThat(result.options())
                    .extracting(ProductResult.Option::optionTypeId, ProductResult.Option::optionTypeName,
                            ProductResult.Option::priority)
                    .containsExactly(
                            tuple(size.getId(), size.getName(), 1),
                            tuple(color.getId(), color.getName(), 2)
                    );
        }

        @Test
        @DisplayName("상품 옵션을 설정할때 상품을 찾을 수 없으면 예외를 던진다")
        void defineOptions_notFound_product(){
            //given
            ProductCommand.OptionRegister command = ProductCommand.OptionRegister.builder()
                    .productId(999L)
                    .optionTypeIds(List.of(1L, 2L))
                    .build();
            //when
            //then
            assertThatThrownBy(() -> productService.defineOptions(command))
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
            ProductCommand.OptionRegister command = ProductCommand.OptionRegister.builder()
                    .productId(product.getId())
                    .optionTypeIds(List.of(size.getId(), 999L))
                    .build();
            //when
            //then
            assertThatThrownBy(() -> productService.defineOptions(command))
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
            ProductCommand.AddVariant command = createVariantCommand()
                    .productId(product.getId()).variants(
                            List.of(
                                    ProductCommand.VariantDetail.builder()
                                            .originalPrice(3000L)
                                            .discountRate(10)
                                            .stockQuantity(100)
                                            .optionValueIds(List.of(xl.getId(), blue.getId())).build()))
                    .build();
            //when
            ProductResult.AddVariant result = productService.createVariants(command);
            //then
            String createdSku = "PROD" + product.getId() + "-" +xl.getName() + "-" + blue.getName();
            assertThat(result.productId()).isEqualTo(product.getId());
            assertThat(result.variants())
                    .allSatisfy(v -> assertThat(v.variantId()).isNotNull());

            assertThat(result.variants())
                    .hasSize(1)
                    .satisfiesExactly(
                            variantResp -> {
                                assertThat(variantResp.variantId()).isNotNull();
                                assertThat(variantResp.sku()).isEqualTo(createdSku);
                                assertThat(variantResp.originalPrice()).isEqualTo(3000L);
                                assertThat(variantResp.discountRate()).isEqualTo(10);
                                assertThat(variantResp.optionValueIds())
                                        .containsExactlyInAnyOrder(xl.getId(), blue.getId());
                            }
                    );
        }

        @Test
        @DisplayName("동일한 옵션 조합의 상품 변형을 여러개 생성할 수 없다 [상품 옵션이 있는 경우]")
        void createVariants_duplicate_variant_request_has_option_case(){
            //given
            ProductCommand.AddVariant command = createVariantCommand().variants(
                    List.of(
                            ProductCommand.VariantDetail.builder()
                                    .originalPrice(3000L)
                                    .discountRate(10)
                                    .stockQuantity(100)
                                    .optionValueIds(List.of(1L, 2L)).build(),
                            ProductCommand.VariantDetail.builder()
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
                    .isEqualTo(ProductErrorCode.VARIANT_DUPLICATED_IN_REQUEST);
        }

        @Test
        @DisplayName("동일한 옵션 조합의 상품 변형을 여러개 생성할 수 없다 [상품 옵션이 없는 경우]")
        void createVariants_duplicate_variant_request_no_option_case(){
            //given
            ProductCommand.AddVariant command = createVariantCommand().variants(
                            List.of(
                                    ProductCommand.VariantDetail.builder()
                                            .originalPrice(3000L)
                                            .discountRate(10)
                                            .stockQuantity(100)
                                            .optionValueIds(List.of()).build(),
                                    ProductCommand.VariantDetail.builder()
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
                    .isEqualTo(ProductErrorCode.VARIANT_DUPLICATED_IN_REQUEST);
        }

        @Test
        @DisplayName("상품을 찾을 수 없으면 예외를 던진다")
        void createVariants_not_found_product(){
            //given
            ProductCommand.AddVariant command = createVariantCommand().productId(999L).build();
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
            ProductCommand.AddVariant command = createVariantCommand()
                    .productId(product.getId()).variants(
                            List.of(
                                    ProductCommand.VariantDetail.builder()
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
            ProductCommand.AddImage command = ProductCommand.AddImage.builder()
                    .productId(999L)
                    .images(List.of("/test/image1.jpg", "/test/image2.jpg"))
                    .build();
            //when
            //then
            assertThatThrownBy(() -> productService.updateImages(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("상품 이미지를 추가한다")
        void addImages(){
            //given
            Category category = saveCategory();
            Product product = Product.create("상품", "상품 설명", category);
            productRepository.save(product);
            ProductCommand.AddImage command = ProductCommand.AddImage.builder()
                    .productId(product.getId())
                    .images(List.of("/test/image1.jpg", "/test/image2.jpg"))
                    .build();
            //when
            ProductResult.AddImage result = productService.updateImages(command);
            //then
            assertThat(result.productId()).isEqualTo(product.getId());
            assertThat(result.images())
                    .allSatisfy(image -> assertThat(image.imageId()).isNotNull())
                    .extracting(ProductResult.ImageDetail::imagePath, ProductResult.ImageDetail::sortOrder, ProductResult.ImageDetail::isThumbnail)
                    .containsExactlyInAnyOrder(
                            tuple("/test/image1.jpg", 1, true),
                            tuple("/test/image2.jpg", 2, false)
                    );
        }
    }

    @Nested
    @DisplayName("상품 설명 이미지 추가")
    class AddDescriptionImages {
        @Test
        @DisplayName("상품을 찾을 수 없으면 예외를 던진다")
        void updateDescriptionImages_notFound_product(){
            //given
            ProductCommand.AddDescriptionImage command = ProductCommand.AddDescriptionImage.builder()
                    .productId(999L)
                    .images(List.of("/test/image1.jpg", "/test/image2.jpg"))
                    .build();
            //when
            //then
            assertThatThrownBy(() -> productService.updateDescriptionImages(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("상품 설명 이미지를 추가한다")
        void updateDescriptionImages(){
            //given
            Category category = saveCategory();
            Product product = Product.create("상품", "상품 설명", category);
            productRepository.save(product);
            ProductCommand.AddDescriptionImage command = ProductCommand.AddDescriptionImage.builder()
                    .productId(product.getId())
                    .images(List.of("/test/image1.jpg", "/test/image2.jpg"))
                    .build();
            //when
            ProductResult.AddDescriptionImage result = productService.updateDescriptionImages(command);
            //then
            assertThat(result.productId()).isEqualTo(product.getId());
            assertThat(result.images())
                    .allSatisfy(image -> assertThat(image.imageId()).isNotNull())
                    .extracting(ProductResult.DescriptionImageDetail::imagePath, ProductResult.DescriptionImageDetail::sortOrder)
                    .containsExactlyInAnyOrder(
                            tuple("/test/image1.jpg", 1),
                            tuple("/test/image2.jpg", 2)
                    );
        }
    }

    @Nested
    @DisplayName("상품 게시")
    class Publish {

        @Test
        @DisplayName("상품을 게시한다")
        void publish(){
            //given
            Category category = saveCategory();
            Product product = Product.create("상품", "상품 설명", category);
            ProductVariant variant = ProductVariant.create("TEST", 3000L, 100, 10);
            product.addVariant(variant);
            product.replaceImages(List.of("/test/image.jpg"));
            product.replaceDescriptionImage(List.of("/test/description.jpg"));
            productRepository.save(product);
            //when
            ProductResult.Publish result = productService.publish(product.getId());
            //then
            assertThat(result.productId()).isEqualTo(product.getId());
            assertThat(result.status()).isEqualTo(ProductStatus.ON_SALE);
            assertThat(result.publishedAt()).isNotNull();
        }

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
    }

    @Nested
    @DisplayName("상품 조회")
    class GetProduct {

        @Test
        @DisplayName("상품 상세 정보를 조회한다")
        void getProduct(){
            //given
            OptionType size = saveOptionType("사이즈", List.of("XL", "L", "M", "S"));
            OptionType color = saveOptionType("색상", List.of("RED", "BLUE"));
            OptionValue xl = findOptionValue(size, "XL");
            OptionValue blue = findOptionValue(color, "BLUE");
            OptionValue red = findOptionValue(color, "RED");
            Category category = saveCategory();
            ProductVariant xl_blue = ProductVariant.create("TEST-XL-BLUE", 10000L, 100, 20);
            ProductVariant xl_red = ProductVariant.create("TEST-XL-RED", 5000L, 100, 10);
            xl_blue.addProductVariantOptions(List.of(xl, blue));
            xl_red.addProductVariantOptions(List.of(xl, red));
            Product product = Product.create("상품", "상품 설명", category);
            product.updateOptions(List.of(size, color));
            product.addVariant(xl_blue);
            product.addVariant(xl_red);
            product.replaceImages(List.of("/test/thumbnail.jpg", "/test/image.jpg"));
            product.replaceDescriptionImage(List.of("/test/description.jpg"));
            product.publish();
            productRepository.save(product);
            //when
            ProductResult.Detail result = productService.getProduct(product.getId());
            //then
            assertThat(result)
                    .extracting(ProductResult.Detail::productId, ProductResult.Detail::name, ProductResult.Detail::status,
                            ProductResult.Detail::displayPrice, ProductResult.Detail::originalPrice, ProductResult.Detail::maxDiscountRate)
                    .containsExactly(product.getId(), "상품", ProductStatus.ON_SALE,
                            4500L, 5000L, 20);
            assertThat(result.optionGroups())
                    .extracting(ProductResult.OptionGroup::optionTypeId, ProductResult.OptionGroup::name, ProductResult.OptionGroup::priority)
                    .containsExactlyInAnyOrder(
                            tuple(size.getId(), size.getName(), 1),
                            tuple(color.getId(), color.getName(), 2)
                    );
            assertThat(result.images())
                    .allSatisfy(image -> assertThat(image.imageId()).isNotNull())
                    .extracting(ProductResult.ImageDetail::imagePath, ProductResult.ImageDetail::sortOrder, ProductResult.ImageDetail::isThumbnail)
                    .containsExactlyInAnyOrder(
                            tuple("/test/thumbnail.jpg", 1, true),
                            tuple("/test/image.jpg", 2, false)
                    );

            assertThat(result.descriptionImages())
                    .allSatisfy(image -> assertThat(image.imageId()).isNotNull())
                    .extracting(ProductResult.DescriptionImageDetail::imagePath, ProductResult.DescriptionImageDetail::sortOrder)
                            .containsExactlyInAnyOrder(
                                    tuple("/test/description.jpg", 1)
                            );

            assertThat(result.variants())
                    .hasSize(2)
                    .satisfiesExactlyInAnyOrder(
                            variantResp1 -> {
                                assertThat(variantResp1.variantId()).isNotNull();
                                assertThat(variantResp1.sku()).isEqualTo("TEST-XL-BLUE");
                                assertThat(variantResp1.originalPrice()).isEqualTo(10000L);
                                assertThat(variantResp1.discountRate()).isEqualTo(20);
                                assertThat(variantResp1.optionValueIds())
                                        .containsExactlyInAnyOrder(xl.getId(), blue.getId());
                            },
                            variantResp2 -> {
                                assertThat(variantResp2.variantId()).isNotNull();
                                assertThat(variantResp2.sku()).isEqualTo("TEST-XL-RED");
                                assertThat(variantResp2.originalPrice()).isEqualTo(5000L);
                                assertThat(variantResp2.discountRate()).isEqualTo(10);
                                assertThat(variantResp2.optionValueIds())
                                        .containsExactlyInAnyOrder(xl.getId(), red.getId());
                            }
                    );
        }

        @Test
        @DisplayName("상품이 존재하지 않으면 예외를 던진다")
        void getProduct_not_found_product(){
            //given
            //when
            //then
            assertThatThrownBy(() -> productService.getProduct(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("상품 목록 조회")
    class GetProducts {

        @Test
        @DisplayName("상품 목록을 조회한다")
        void getProducts() {
            //given
            Category category = saveCategory();
            Product product1 = settingProduct(category, "상품1");
            Product product2 = settingProduct(category, "상품2");
            Pageable pageable = PageRequest.of(0, 10);
            ProductCommand.Search command = ProductCommand.Search.builder()
                    .pageable(pageable)
                    .build();
            //when
            Page<ProductResult.Summary> result = productService.getProducts(command);
            //then
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getContent())
                    .extracting(ProductResult.Summary::productId, ProductResult.Summary::name)
                    .containsExactly(
                            tuple(product2.getId(), product2.getName()),
                            tuple(product1.getId(), product1.getName())
                    );
        }
    }

    @Nested
    @DisplayName("상품 정보를 수정한다")
    class UpdateProduct {

        @Test
        @DisplayName("상품을 찾을 수 없으면 예외를 던진다")
        void updateProduct_product_not_found() {
            //given
            ProductCommand.Update command = ProductCommand.Update.builder()
                    .productId(999L)
                    .name("새 이름")
                    .description("새 설명")
                    .categoryId(1L)
                    .build();
            //when
            //then
            assertThatThrownBy(() -> productService.updateProduct(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("카테고리를 찾을 수 없으면 예외를 던진다")
        void updateProduct_category_not_found() {
            //given
            Category category = saveCategory();
            Product product = saveProduct(category);
            ProductCommand.Update command = ProductCommand.Update.builder()
                    .productId(product.getId())
                    .name("새 이름")
                    .description("새 설명")
                    .categoryId(999L)
                    .build();
            //when
            //then
            assertThatThrownBy(() -> productService.updateProduct(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("상품 정보 수정")
        void updateProduct() {
            //given
            Category category = saveCategory();
            Category newCategory = saveCategory();
            Product product = saveProduct(category);
            ProductCommand.Update command = ProductCommand.Update.builder()
                    .productId(product.getId())
                    .name("새 이름")
                    .description("새 설명")
                    .categoryId(newCategory.getId())
                    .build();
            //when
            ProductResult.Update result = productService.updateProduct(command);
            //then
            assertThat(result.productId()).isEqualTo(product.getId());
            assertThat(result.categoryId()).isEqualTo(newCategory.getId());
            assertThat(result.name()).isEqualTo("새 이름");
            assertThat(result.description()).isEqualTo("새 설명");
        }
    }

    @Nested
    @DisplayName("상품 삭제")
    class Delete {

        @Test
        @DisplayName("상품을 삭제한다")
        void deleteProduct() {
            //given
            Category category = saveCategory();
            Product product = saveProduct(category);
            //when
            product.deleted();
            //then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DELETED);
        }

        @Test
        @DisplayName("삭제할 상품을 찾을 수 없으면 예외를 던진다")
        void deleteProduct_product_not_found() {
            //given
            //when
            //then
            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("상품 판매 중지")
    class Close {

        @Test
        @DisplayName("상품을 판매 중지로 변경한다")
        void closeProduct() {
            //given
            Category category = saveCategory();
            Product product = Product.create("상품", "상품 설명", category);
            ProductVariant variant = ProductVariant.create("TEST", 3000L, 100, 10);
            product.addVariant(variant);
            product.replaceImages(List.of("/test/image.jpg"));
            product.replaceDescriptionImage(List.of("/test/description.jpg"));
            product.publish();
            productRepository.save(product);
            //when
            ProductResult.Close result = productService.closedProduct(product.getId());
            //then
            assertThat(result.productId()).isEqualTo(product.getId());
            assertThat(result.status()).isEqualTo(ProductStatus.STOP_SALE);
            assertThat(result.saleStoppedAt()).isNotNull();
        }
    }
}
