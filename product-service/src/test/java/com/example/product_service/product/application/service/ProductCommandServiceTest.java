package com.example.product_service.product.application.service;

import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.common.exception.ProductErrorCode;
import com.example.product_service.common.util.IdGenerator;
import com.example.product_service.product.application.port.ProductCategoryPort;
import com.example.product_service.product.application.port.ProductRepository;
import com.example.product_service.product.application.port.dto.ProductCategoryResult;
import com.example.product_service.product.application.service.dto.command.CreateProductCommand;
import com.example.product_service.product.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.product_service.product.fixture.ProductCommandFixture.anCreateProductCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProductCommandServiceTest {

    @InjectMocks
    private ProductCommandService productCommandService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCategoryPort productCategoryPort;

    @Mock
    private IdGenerator idGenerator;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    @Test
    @DisplayName("상품을 생성한다")
    void createProduct() {
        //given
        Long expectedId = 100L;
        CreateProductCommand command = anCreateProductCommand().build();
        ProductCategoryResult category = new ProductCategoryResult(command.categoryId(), "카테고리", true);

        given(productCategoryPort.getCategory(command.categoryId())).willReturn(category);
        given(idGenerator.generate()).willReturn(expectedId);
        given(productRepository.save(any(Product.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        //when
        Long productId = productCommandService.createProduct(command);
        //then
        assertThat(productId).isEqualTo(expectedId);

        then(productRepository).should().save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();
        assertThat(savedProduct.getId()).isEqualTo(expectedId);
        assertThat(savedProduct.getName()).isEqualTo(command.name());
        assertThat(savedProduct.getCategoryId()).isEqualTo(command.categoryId());
        assertThat(savedProduct.getDescription()).isEqualTo(command.description());
    }

    @Test
    @DisplayName("카테고리가 리프 카테고리가 아니면 예외가 발생하고 상품을 생성하지 않는다")
    void createProduct_whenCategoryNotLeaf_thenThrownException() {
        //given
        CreateProductCommand command = anCreateProductCommand().build();
        ProductCategoryResult category = new ProductCategoryResult(command.categoryId(), "카테고리", false);

        given(productCategoryPort.getCategory(command.categoryId())).willReturn(category);
        //when
        //then
        assertThatThrownBy(() -> productCommandService.createProduct(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.CATEGORY_NOT_LEAF);

        then(productRepository).should(never()).save(any());
    }
}
