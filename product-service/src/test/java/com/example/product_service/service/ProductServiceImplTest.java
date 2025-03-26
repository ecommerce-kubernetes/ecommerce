package com.example.product_service.service;

import com.example.product_service.dto.request.ProductRequestDto;
import com.example.product_service.dto.request.StockQuantityRequestDto;
import com.example.product_service.dto.response.ProductResponseDto;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoriesRepository;
import com.example.product_service.repository.ProductsRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Slf4j
class ProductServiceImplTest {

    @Autowired
    ProductService productService;

    @Autowired
    ProductsRepository productsRepository;
    @Autowired
    CategoriesRepository categoriesRepository;

    private Categories food;
    private Products banana;

    @BeforeEach
    void initDB(){
        food = categoriesRepository.save(new Categories("식품"));
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
    void modifyStockQuantity(){
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
    void modifyStockQuantity_NotFoundProduct(){
        assertThatThrownBy(() -> productService.modifyStockQuantity(999L, new StockQuantityRequestDto(40)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Product");
    }

}