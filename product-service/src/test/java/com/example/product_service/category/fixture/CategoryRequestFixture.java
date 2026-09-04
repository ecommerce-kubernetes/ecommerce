package com.example.product_service.category.fixture;

import com.example.product_service.category.adapter.in.web.dto.request.CreateCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.MoveCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.UpdateCategoryRequest;

public class CategoryRequestFixture {
    public static CreateCategoryRequest.CreateCategoryRequestBuilder anCreateCategoryRequest() {
        return CreateCategoryRequest.builder()
                .name("전자기기")
                .imagePath("/product/electronics.jpg");
    }

    public static UpdateCategoryRequest.UpdateCategoryRequestBuilder anUpdateCategoryRequest() {
        return UpdateCategoryRequest.builder()
                .name("전자기기")
                .imagePath("/product/electronics.jpg");
    }

    public static MoveCategoryRequest.MoveCategoryRequestBuilder anMoveCategoryRequest() {
        return MoveCategoryRequest.builder()
                .newParentId(2L);
    }
}
