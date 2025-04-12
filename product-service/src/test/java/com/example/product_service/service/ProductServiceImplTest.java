package com.example.product_service.service;

import com.example.product_service.dto.request.ProductRequestDto;
import com.example.product_service.dto.request.ProductRequestIdsDto;
import com.example.product_service.dto.request.StockQuantityRequestDto;
import com.example.product_service.dto.response.CompactProductResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ProductResponseDto;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoriesRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.kafka.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Slf4j
class ProductServiceImplTest {

    @Autowired
    ProductService productService;
    @Autowired
    ProductsRepository productsRepository;
    @Autowired
    CategoriesRepository categoriesRepository;

    @MockitoBean
    KafkaProducer kafkaProducer;

    private Categories food;
    private Categories electronicDevices;

    @BeforeEach
    void initDB(){
        food = categoriesRepository.save(new Categories("식품"));
        electronicDevices = categoriesRepository.save(new Categories("전자기기"));
    }

    @AfterEach
    void clearDB(){
        productsRepository.deleteAll();
    }

    @Test
    @Transactional
    @DisplayName("상품 저장 테스트")
    void saveProductTest(){
        ProductRequestDto productRequestDto = new ProductRequestDto("사과", "청송 사과 3EA",5000, 50, food.getId());
        ProductResponseDto productResponseDto = productService.saveProduct(productRequestDto);

        assertThat(productResponseDto.getName()).isEqualTo("사과");
        assertThat(productResponseDto.getDescription()).isEqualTo("청송 사과 3EA");
        assertThat(productResponseDto.getPrice()).isEqualTo(5000);
        assertThat(productResponseDto.getStockQuantity()).isEqualTo(50);
        assertThat(productResponseDto.getCategoryId()).isEqualTo(food.getId());
    }

    @Test
    @Transactional
    @DisplayName("상품 저장 테스트 - 카테고리를 찾을 수 없는 경우")
    void saveProductTest_NotFoundCategories(){
        ProductRequestDto productRequestDto = new ProductRequestDto("사과", "청송 사과 3EA", 5000, 50, 999L);
        assertThatThrownBy(() ->  productService.saveProduct(productRequestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Category");
    }

    @Test
    @DisplayName("상품 삭제 테스트")
    void deleteProductTest(){
        Products banana = productsRepository.save(
                new Products("바나나", "바나나 3개입", 5000, 50, food)
        );
        productService.deleteProduct(banana.getId());

        Optional<Products> bananaOptional = productsRepository.findById(banana.getId());
        assertThat(bananaOptional).isEmpty();
        verify(kafkaProducer).sendMessage(anyString(), any());
    }

    @Test
    @DisplayName("상품 삭제 테스트 - 없는 상품을 삭제하는 경우")
    void deleteProductTest_NotFoundProduct(){
        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(NotFoundException.class)
                        .hasMessage("Not Found Product");
    }

    @Test
    @DisplayName("상품 재고 변경 테스트")
    @Transactional
    void modifyStockQuantityTest(){
        Products banana = productsRepository.save(
                new Products("바나나", "바나나 3개입", 5000, 50, food)
        );

        ProductResponseDto productResponseDto =
                productService.modifyStockQuantity(banana.getId(), new StockQuantityRequestDto(40));

        assertThat(productResponseDto.getStockQuantity()).isEqualTo(40);

        Products products = productsRepository.findById(banana.getId()).orElseThrow();
        assertThat(products.getStockQuantity()).isEqualTo(40);
    }

    @Test
    @DisplayName("상품 재고 변경 테스트 - 없는 상품 재고 변경")
    void modifyStockQuantityTest_NotFoundProduct(){
        assertThatThrownBy(() -> productService.modifyStockQuantity(999L, new StockQuantityRequestDto(40)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Product");
    }

    @Test
    @DisplayName("상품 정보 조회(상품 아이디)")
    void getProductDetailsTest(){
        Products save = productsRepository.save(
                new Products("바나나", "바나나 3개입", 5000, 50, food)
        );

        ProductResponseDto productDetails = productService.getProductDetails(save.getId());

        assertThat(productDetails.getId()).isEqualTo(save.getId());
        assertThat(productDetails.getName()).isEqualTo(save.getName());
        assertThat(productDetails.getDescription()).isEqualTo(save.getDescription());
        assertThat(productDetails.getPrice()).isEqualTo(save.getPrice());
        assertThat(productDetails.getStockQuantity()).isEqualTo(save.getStockQuantity());
        assertThat(productDetails.getCategoryId()).isEqualTo(save.getCategory().getId());
    }

    @ParameterizedTest
    @CsvSource({
            "'', ''",
            "'1', ''",
            "'', '사과'",
            "'1', '사과'"
    })
    @DisplayName("상품 리스트 조회")
    @Transactional
    void getAllProductsTest(String categoryIdStr, String name) {
        Long categoryId = categoryIdStr.isEmpty() ? null : food.getId();
        List<Products> list = new ArrayList<>();
        Products apple = new Products("사과", "청송 사과 3EA", 5000, 50, food);
        Products banana = new Products("바나나", "바나나 3개입", 5000, 50, food);
        Products pineApple = new Products("파인애플", "파인애플 5개입", 6000, 50, food);
        Products iphone = new Products("아이폰 16", "애플 아이폰 16", 1250000, 50, electronicDevices);
        list.add(apple);
        list.add(banana);
        list.add(pineApple);
        list.add(iphone);

        productsRepository.saveAll(list);
        List<Products> expectedList = list.stream()
                .filter(product -> categoryId == null || product.getCategory().getId().equals(categoryId))
                .filter(product -> name.isEmpty() || product.getName().contains(name))
                .toList();

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "id");

        PageDto<ProductResponseDto> pageDto = productService.getProductList(pageable, categoryId, name);

        assertThat(pageDto.getCurrentPage()).isEqualTo(0);
        assertThat(pageDto.getPageSize()).isEqualTo(10);
        assertThat(pageDto.getTotalPage()).isEqualTo(1);
        assertThat(pageDto.getTotalElement()).isEqualTo(expectedList.size());

        List<ProductResponseDto> content = pageDto.getContent();
        for (int i = 0; i < content.size(); i++) {
            ProductResponseDto actual = content.get(i);
            Products expected = expectedList.get(i);
            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
            assertThat(actual.getPrice()).isEqualTo(expected.getPrice());
            assertThat(actual.getStockQuantity()).isEqualTo(expected.getStockQuantity());
            assertThat(actual.getCategoryId()).isEqualTo(expected.getCategory().getId());
        }
    }

    @Test
    @Transactional
    void getProductListByIdsTest(){
        Products banana = productsRepository.save(new Products("바나나", "바나나 3개입", 5000, 50, food));
        Products apple = productsRepository.save(new Products("사과", "사과 3개입", 5000, 50, food));
        Products iphone = productsRepository.save(new Products("아이폰 16", "애플 아이폰 16", 1250000, 50, electronicDevices));

        ProductRequestIdsDto productRequestIdsDto = new ProductRequestIdsDto(List.of(banana.getId(), apple.getId()));

        List<CompactProductResponseDto> productListByIds = productService.getProductListByIds(productRequestIdsDto);

        assertThat(productListByIds.size()).isEqualTo(2);

    }

    @Test
    @Transactional
    void getProductListByIdsTest_NotFoundProduct(){
        Products banana = productsRepository.save(new Products("바나나", "바나나 3개입", 5000, 50, food));
        Products apple = productsRepository.save(new Products("사과", "사과 3개입", 5000, 50, food));
        Products iphone = productsRepository.save(new Products("아이폰 16", "애플 아이폰 16", 1250000, 50, electronicDevices));

        ProductRequestIdsDto productRequestIdsDto = new ProductRequestIdsDto(List.of(banana.getId(), 999L));
        List<Long> notFoundId = List.of(999L);
        assertThatThrownBy(() -> productService.getProductListByIds(productRequestIdsDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found product by id:" + notFoundId);

    }

}