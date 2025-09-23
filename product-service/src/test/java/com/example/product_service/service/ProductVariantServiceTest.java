package com.example.product_service.service;

import com.example.product_service.common.MessagePath;
import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.*;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.InsufficientStockException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.OptionTypeRepository;
import com.example.product_service.repository.ProductVariantsRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.dto.InventoryReductionItem;
import com.example.product_service.util.TestMessageUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductVariantServiceTest {
    @Autowired
    ProductsRepository productsRepository;
    @Autowired
    OptionTypeRepository optionTypeRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductVariantsRepository productVariantsRepository;
    @Autowired
    ProductVariantService productVariantService;
    @Autowired
    EntityManager em;
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
    @DisplayName("상품 변형 수정 테스트-성공")
    void updateVariantByIdTest_integration_success(){
        ProductImage productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionType productOptionType = createProductOptionType(storage);
        ProductVariant productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Product product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);
        em.flush(); em.clear();

        UpdateProductVariantRequest request = new UpdateProductVariantRequest(50000, 100, 10);

        ProductVariantResponse response = productVariantService.updateVariantById(productVariant.getId(), request);

        assertThat(response.getId()).isEqualTo(productVariant.getId());
        assertThat(response.getPrice()).isEqualTo(50000);
        assertThat(response.getStockQuantity()).isEqualTo(100);
        assertThat(response.getDiscountRate()).isEqualTo(10);

        em.flush(); em.clear();

        ProductVariant findProductVariant = productVariantsRepository.findById(productVariant.getId()).get();
        assertThat(findProductVariant.getId()).isEqualTo(productVariant.getId());
        assertThat(findProductVariant.getPrice()).isEqualTo(50000);
        assertThat(findProductVariant.getStockQuantity()).isEqualTo(100);
        assertThat(findProductVariant.getDiscountValue()).isEqualTo(10);
    }

    @Test
    @DisplayName("상품 변형 수정 테스트-실패(상품 변형을 찾을 수 없음)")
    void updateVariantByIdTest_integration_notFound_productVariant(){
        UpdateProductVariantRequest request = new UpdateProductVariantRequest(50000, 100, 10);
        assertThatThrownBy(() -> productVariantService.updateVariantById(999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(TestMessageUtil.getMessage(MessagePath.PRODUCT_VARIANT_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 변형 삭제 테스트-성공")
    void deleteVariantByIdTest_integration_success(){
        ProductImage productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionType productOptionType = createProductOptionType(storage);
        ProductVariant productVariant1 = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);
        ProductVariant productVariant2 = createProductVariants("IPHONE16-256GB", 10000, 100, 10, gb_256);
        Product product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant1, productVariant2));

        productsRepository.save(product);
        productVariantService.deleteVariantById(productVariant2.getId());
        em.flush(); em.clear();

        Product findProduct = productsRepository.findById(product.getId()).get();

        assertThat(findProduct.getProductVariants().size()).isEqualTo(1);
        assertThat(findProduct.getProductVariants())
                .extracting(ProductVariant::getId, ProductVariant::getSku, ProductVariant::getPrice,
                        ProductVariant::getStockQuantity, ProductVariant::getDiscountValue)
                .containsExactlyInAnyOrder(
                        tuple(productVariant1.getId(), productVariant1.getSku(), productVariant1.getPrice(),
                                productVariant1.getStockQuantity(), productVariant1.getDiscountValue())
                );

        assertThat(product.getProductVariants())
                .flatExtracting(ProductVariant::getProductVariantOptions)
                .extracting(pvo -> pvo.getOptionValue().getOptionValue())
                .containsExactlyInAnyOrder( "128GB");

    }

    @Test
    @DisplayName("상품 변형 삭제 테스트-실패(상품 변형 찾을 수 없음)")
    void deleteVariantByIdTest_integration_notFound_productVariant(){

        assertThatThrownBy(() -> productVariantService.deleteVariantById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(TestMessageUtil.getMessage(MessagePath.PRODUCT_VARIANT_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 변형 삭제 테스트-실패(상품에 존재하는 상품 변형의 개수가 1개일때)")
    void deleteVariantByIdTest_integration_variant_minimum_number(){
        ProductImage productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionType productOptionType = createProductOptionType(storage);
        ProductVariant productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Product product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);
        em.flush(); em.clear();

        assertThatThrownBy(() -> productVariantService.deleteVariantById(productVariant.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("must be at least one product variant per product");
    }

    @Test
    @DisplayName("재고감소-성공")
    void inventoryReductionByIdTest_integration_success(){
        ProductImage productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionType productOptionType = createProductOptionType(storage);
        ProductVariant productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);
        Product product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);
        em.flush(); em.clear();

        List<InventoryReductionItem> inventoryReductionItem = productVariantService.inventoryReductionById(Map.of(productVariant.getId(), 10));
        assertThat(inventoryReductionItem)
                .extracting(InventoryReductionItem::getProductVariantId, InventoryReductionItem::getPrice,
                        InventoryReductionItem::getStock, InventoryReductionItem::getDiscountPrice)
                .containsExactlyInAnyOrder(
                        tuple(productVariant.getId(), productVariant.getPrice(), 10,
                                productVariant.getDiscountPrice()));
        em.flush();
        em.clear();

        ProductVariant result = productVariantsRepository.findById(productVariant.getId()).get();
        assertThat(result.getStockQuantity()).isEqualTo(90);
    }

    @Test
    @DisplayName("재고 감소-실패(상품 변형 찾을 수 없음)")
    void inventoryReductionByIdTest_integration_notFound_productVariant(){
        assertThatThrownBy(() -> productVariantService.inventoryReductionById(Map.of(999L, 10)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(TestMessageUtil.getMessage(MessagePath.PRODUCT_VARIANT_NOT_FOUND));
    }

    @Test
    @DisplayName("재고 감소-실패(재고 부족)")
    void inventoryReductionByIdTest_integration_outOfStock(){
        ProductImage productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionType productOptionType = createProductOptionType(storage);
        ProductVariant productVariant = createProductVariants("IPHONE16-128GB", 10000, 5, 10, gb_128);
        Product product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);
        em.flush(); em.clear();

        assertThatThrownBy(() -> productVariantService.inventoryReductionById(Map.of(productVariant.getId(), 10)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("Out of Stock");
    }

    @Test
    @DisplayName("재고 복원 테스트-성공")
    void inventoryRestorationByIdTest_integration_success(){
        ProductImage productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionType productOptionType = createProductOptionType(storage);
        ProductVariant productVariant = createProductVariants("IPHONE16-128GB", 10000, 90, 10, gb_128);

        Product product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);
        em.flush(); em.clear();

        productVariantService.inventoryRestorationById(Map.of(productVariant.getId(), 10));

        em.flush();
        em.clear();

        ProductVariant result = productVariantsRepository.findById(productVariant.getId()).get();
        assertThat(result.getStockQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("재고 복원 테스트-실패(상품 변형을 찾을 수 없음)")
    void inventoryRestorationByIdTest_integration_notFound_productVariant(){
        assertThatThrownBy(() -> productVariantService.inventoryRestorationById(Map.of(999L, 10)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(TestMessageUtil.getMessage(MessagePath.PRODUCT_VARIANT_NOT_FOUND));
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