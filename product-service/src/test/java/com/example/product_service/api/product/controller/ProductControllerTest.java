package com.example.product_service.api.product.controller;

import com.example.product_service.api.product.controller.dto.ProductCreateRequest;
import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import com.example.product_service.api.product.service.dto.result.ProductCreateResponse;
import com.example.product_service.support.ControllerTestSupport;
import com.example.product_service.support.security.annotation.WithCustomMockUser;
import com.example.product_service.support.security.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import static com.example.product_service.support.fixture.ProductControllerFixture.mockCreateRequest;
import static com.example.product_service.support.fixture.ProductControllerFixture.mockCreateResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
public class ProductControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("상품을 생성한다")
    @WithCustomMockUser
    void createProduct() throws Exception {
        //given
        ProductCreateRequest request = mockCreateRequest().build();
        ProductCreateResponse response = mockCreateResponse().build();
        given(productService.createProduct(any(ProductCreateCommand.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("")
    void createProduct_unAuthentication() {
        //given
        //when
        //then
    }
}
