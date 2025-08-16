package com.example.product_service.controller.auth;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.common.advice.CustomAccessDeniedHandler;
import com.example.product_service.common.advice.CustomAuthenticationEntryPoint;
import com.example.product_service.config.WebSecurity;
import com.example.product_service.controller.ProductController;
import com.example.product_service.util.UserRole;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

import static com.example.product_service.util.ControllerTestHelper.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(value = ProductController.class, excludeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = RestControllerAdvice.class
))
@Import({WebSecurity.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class, MessageSourceUtil.class})
@AutoConfigureMockMvc
public class ProductControllerSecurityTest {

    private static final String BASE_PATH = "/products";
    private static final String PRODUCT_ID_PATH = BASE_PATH + "/1";
    private static final String PRODUCT_IMAGE_PATH = BASE_PATH + "/1/images";
    private static final String PRODUCT_IMAGE_BULK_PATH = PRODUCT_IMAGE_PATH + "/bulk";
    private static final String PRODUCT_VARIANT_PATH = BASE_PATH + "/1/variants";
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    ProductService productService;

    @Test
    @DisplayName("상품 저장 테스트-인증 에러")
    void createProductTest_UnAuthorized() throws Exception {
        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), createProductRequest());
        verifyUnauthorizedResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("상품 저장 테스트-권한 부족")
    void createProductTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, post(BASE_PATH), createProductRequest(), UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("상품 이미지 추가 테스트-인증 에러")
    void addImageTest_UnAuthorized() throws Exception {
        ResultActions perform =
                performWithBody(mockMvc, post(PRODUCT_IMAGE_BULK_PATH), createAddImageRequest());
        verifyUnauthorizedResponse(perform, PRODUCT_IMAGE_BULK_PATH);
    }

    @Test
    @DisplayName("상품 이미지 추가 테스트-권한 부족")
    void addImageTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, post(PRODUCT_IMAGE_BULK_PATH), createAddImageRequest(), UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, PRODUCT_IMAGE_BULK_PATH);
    }

    @Test
    @DisplayName("상품 변형 추가 테스트-인증 에러")
    void addVariantTest_UnAuthorized() throws Exception {
        ResultActions perform = performWithBody(mockMvc, post(PRODUCT_VARIANT_PATH), createProductVariantRequest());
        verifyUnauthorizedResponse(perform, PRODUCT_VARIANT_PATH);
    }

    @Test
    @DisplayName("상품 변형 추가 테스트-권한 부족")
    void addVariantTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, post(PRODUCT_VARIANT_PATH), createProductVariantRequest(), UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, PRODUCT_VARIANT_PATH);
    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-인증 에러")
    void updateBasicInfoTest_UnAuthorized() throws Exception {
        ResultActions perform =
                performWithBody(mockMvc, patch(PRODUCT_ID_PATH), createUpdateProductBasicRequest());
        verifyUnauthorizedResponse(perform, PRODUCT_ID_PATH);
    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-권한 부족")
    void updateBasicInfoTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, patch(PRODUCT_ID_PATH), createUpdateProductBasicRequest(), UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, PRODUCT_ID_PATH);
    }

    @Test
    @DisplayName("상품 삭제 테스트-인증 에러")
    void deleteProductTest_UnAuthorized() throws Exception {
        ResultActions perform =
                performWithBody(mockMvc, delete(PRODUCT_ID_PATH), createUpdateProductBasicRequest());
        verifyUnauthorizedResponse(perform, PRODUCT_ID_PATH);
    }

    @Test
    @DisplayName("상품 삭제 테스트-권한 부족")
    void deleteProductTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, delete(PRODUCT_ID_PATH), createUpdateProductBasicRequest(), UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, PRODUCT_ID_PATH);
    }


    private ProductRequest createProductRequest(){
        return new ProductRequest(
                "name",
                "description",
                1L,
                createImageRequestList(),
                createProductOptionTypeRequestList(),
                createProductVariantRequestList()
        );
    }

    private UpdateProductBasicRequest createUpdateProductBasicRequest(){
        return new UpdateProductBasicRequest(
                "name",
                "description",
                1L
        );
    }

    private List<ImageRequest> createImageRequestList(){
        return List.of(new ImageRequest("http://test.jpg"));
    }

    private List<ProductOptionTypeRequest> createProductOptionTypeRequestList(){
        return List.of(new ProductOptionTypeRequest(1L, 0));
    }

    private List<ProductVariantRequest> createProductVariantRequestList(){
        return List.of(new ProductVariantRequest("sku",
                1, 1, 0,
                createVariantOptionRequest()));
    }

    private List<VariantOptionValueRequest> createVariantOptionRequest(){
        return List.of(new VariantOptionValueRequest(1L, 1L));
    }
    private AddImageRequest createAddImageRequest(){
        return new AddImageRequest(List.of("http://test1.jpg", "http://test2.jpg"));
    }
    private ProductVariantRequest createProductVariantRequest(){
        return new ProductVariantRequest("sku", 100, 100, 10, List.of(
                new VariantOptionValueRequest(1L, 1L),
                new VariantOptionValueRequest(2L,5L)
        ));
    }
}
