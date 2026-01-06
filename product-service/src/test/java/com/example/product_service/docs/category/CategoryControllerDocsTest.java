package com.example.product_service.docs.category;

import com.example.product_service.api.category.controller.CategoryController;
import com.example.product_service.docs.RestDocsSupport;
import com.example.product_service.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class CategoryControllerDocsTest extends RestDocsSupport {

    CategoryService categoryService = mock(CategoryService.class);

    @Override
    protected Object initController() {
        return new CategoryController(categoryService);
    }

    @Test
    @DisplayName("")
    void getCategoryTree(){
        //given
        //when
        //then
    }

    @Test
    @DisplayName("")
    void getCategory(){
        //given
        //when
        //then
    }
}
