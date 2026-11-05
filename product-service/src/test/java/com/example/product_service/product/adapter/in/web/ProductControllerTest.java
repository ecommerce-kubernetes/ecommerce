package com.example.product_service.product.adapter.in.web;

import com.example.product_service.product.application.service.ProductQueryService;
import com.example.product_service.product.application.service.dto.command.SearchProductCommand;
import com.example.product_service.product.application.service.dto.result.ProductDetailResult;
import com.example.product_service.product.application.service.dto.result.ProductSummaryResult;
import com.example.product_service.support.security.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.example.product_service.product.fixture.ProductResultFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
@WebMvcTest(controllers = ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductQueryService productQueryService;

    @Test
    @DisplayName("상품 목록을 조회한다")
    void getProducts() throws Exception {
        //given
        ProductSummaryResult summary = aProductSummaryResult().build();
        Page<ProductSummaryResult> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1);
        given(productQueryService.getProducts(any(SearchProductCommand.class))).willReturn(page);
        //when
        //then
        mockMvc.perform(get("/products")
                        .param("categoryId", "1")
                        .param("name", "나이키")
                        .param("rating", "3")
                        .param("sort", "latest")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].productId").value(String.valueOf(summary.productId())))
                .andExpect(jsonPath("$.content[0].name").value(summary.name()))
                .andExpect(jsonPath("$.content[0].thumbnail").value(summary.thumbnail()))
                .andExpect(jsonPath("$.content[0].originalPrice").value(summary.originalPrice()))
                .andExpect(jsonPath("$.content[0].discountRate").value(summary.discountRate()))
                .andExpect(jsonPath("$.content[0].price").value(summary.price()))
                .andExpect(jsonPath("$.content[0].reviewCount").value(summary.reviewCount()))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(jsonPath("$.totalElement").value(1));

        var captor = ArgumentCaptor.forClass(SearchProductCommand.class);
        verify(productQueryService).getProducts(captor.capture());
        SearchProductCommand command = captor.getValue();
        assertThat(command.categoryId()).isEqualTo(1L);
        assertThat(command.name()).isEqualTo("나이키");
        assertThat(command.rating()).isEqualTo(3);
        assertThat(command.sort()).isEqualTo("latest");
        assertThat(command.pageable().getPageNumber()).isEqualTo(0);
        assertThat(command.pageable().getPageSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("쿼리 파라미터를 생략하면 기본값으로 조회한다")
    void getProducts_withDefaultParams() throws Exception {
        //given
        ProductSummaryResult summary = aProductSummaryResult().build();
        Page<ProductSummaryResult> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1);
        given(productQueryService.getProducts(any(SearchProductCommand.class))).willReturn(page);
        //when
        //then
        mockMvc.perform(get("/products"))
                .andDo(print())
                .andExpect(status().isOk());

        var captor = ArgumentCaptor.forClass(SearchProductCommand.class);
        verify(productQueryService).getProducts(captor.capture());
        SearchProductCommand command = captor.getValue();
        assertThat(command.categoryId()).isNull();
        assertThat(command.name()).isNull();
        assertThat(command.rating()).isNull();
        assertThat(command.sort()).isEqualTo("latest");
        assertThat(command.pageable().getPageNumber()).isEqualTo(0);
        assertThat(command.pageable().getPageSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("상품 상세를 조회한다")
    void getProduct() throws Exception {
        //given
        ProductDetailResult detail = aProductDetailResult().build();
        given(productQueryService.getProduct(anyLong())).willReturn(detail);
        //when
        //then
        mockMvc.perform(get("/products/{productId}", detail.productId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(detail.productId()))
                .andExpect(jsonPath("$.name").value(detail.name()))
                .andExpect(jsonPath("$.description").value(detail.description()))
                .andExpect(jsonPath("$.status").value(detail.status().name()))
                .andExpect(jsonPath("$.category.id").value(detail.category().id()))
                .andExpect(jsonPath("$.category.name").value(detail.category().name()))
                .andExpect(jsonPath("$.images").isArray())
                .andExpect(jsonPath("$.images[0]").value(detail.images().get(0)))
                .andExpect(jsonPath("$.descriptionImages[0]").value(detail.descriptionImages().get(0)))
                .andExpect(jsonPath("$.rating").value(detail.rating()))
                .andExpect(jsonPath("$.reviewCount").value(detail.reviewCount()))
                .andExpect(jsonPath("$.originalPrice").value(detail.originalPrice()))
                .andExpect(jsonPath("$.discountRate").value(detail.discountRate()))
                .andExpect(jsonPath("$.price").value(detail.price()))
                .andExpect(jsonPath("$.options[0].optionTypeId").value(detail.options().get(0).optionTypeId()))
                .andExpect(jsonPath("$.options[0].optionTypeName").value(detail.options().get(0).optionTypeName()))
                .andExpect(jsonPath("$.options[0].values[0].optionValueId").value(detail.options().get(0).values().get(0).optionValueId()))
                .andExpect(jsonPath("$.options[0].values[0].name").value(detail.options().get(0).values().get(0).name()))
                .andExpect(jsonPath("$.variants[0].variantId").value(detail.variants().get(0).variantId()))
                .andExpect(jsonPath("$.variants[0].sku").value(detail.variants().get(0).sku()))
                .andExpect(jsonPath("$.variants[0].originalPrice").value(detail.variants().get(0).originalPrice()))
                .andExpect(jsonPath("$.variants[0].discountRate").value(detail.variants().get(0).discountRate()))
                .andExpect(jsonPath("$.variants[0].price").value(detail.variants().get(0).price()))
                .andExpect(jsonPath("$.variants[0].stockQuantity").value(detail.variants().get(0).stockQuantity()));
    }
}
