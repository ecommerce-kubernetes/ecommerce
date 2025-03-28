package com.example.product_service.controller;

import com.example.product_service.dto.request.CategoryRequestDto;
import com.example.product_service.dto.response.CategoryResponseDto;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    CategoryService categoryService;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Category 생성 테스트")
    void createCategoryTest() throws Exception {
        CategoryRequestDto categoryRequestDto = new CategoryRequestDto("식품");
        CategoryResponseDto categoryResponseDto = new CategoryResponseDto(1L, "식품");

        when(categoryService.saveCategory(any(CategoryRequestDto.class))).thenReturn(categoryResponseDto);

        String requestBody = mapper.writeValueAsString(categoryRequestDto);
        ResultActions perform = mockMvc.perform(post("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        perform
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(categoryRequestDto.getName()));
    }

    @Test
    @DisplayName("Category 생성 테스트 - 입력값 검증 테스트")
    void createCategoryTest_InvalidCategoryRequestDto() throws Exception {
        CategoryRequestDto categoryRequestDto = new CategoryRequestDto();

        String requestBody = mapper.writeValueAsString(categoryRequestDto);
        ResultActions perform = mockMvc.perform(post("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BadRequest"))
                .andExpect(jsonPath("$.message").value("Validation Error"))
                .andExpect(jsonPath("$.errors[*].fieldName").value("name"))
                .andExpect(jsonPath("$.errors[*].message").value("Category name is required"))
                .andExpect(jsonPath("$.path").value("/categories"));
    }

    @Test
    @DisplayName("Category 이름 변경 테스트")
    void updateCategoryNameTest() throws Exception {
        CategoryRequestDto categoryRequestDto = new CategoryRequestDto("전자기기");
        CategoryResponseDto categoryResponseDto = new CategoryResponseDto(1L, "전자기기");
        String requestBody = mapper.writeValueAsString(categoryRequestDto);

        when(categoryService.modifyCategory(any(Long.class), any(CategoryRequestDto.class))).thenReturn(categoryResponseDto);

        ResultActions perform = mockMvc.perform(patch("/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryResponseDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryRequestDto.getName()));
    }

    @Test
    @DisplayName("Category 이름 변경 테스트 - 입력값 검증 테스트")
    void updateCategoryNameTest_InvalidCategoryRequestDto() throws Exception {
        CategoryRequestDto categoryRequestDto = new CategoryRequestDto();
        String requestBody = mapper.writeValueAsString(categoryRequestDto);

        ResultActions perform = mockMvc.perform(patch("/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BadRequest"))
                .andExpect(jsonPath("$.message").value("Validation Error"))
                .andExpect(jsonPath("$.errors[*].fieldName").value("name"))
                .andExpect(jsonPath("$.errors[*].message").value("Category name is required"))
                .andExpect(jsonPath("$.path").value("/categories/1"));
    }

    @Test
    @DisplayName("Category 이름 변경 테스트 - 카테고리를 찾을 수 없는 경우")
    void updateCategoryNameTest_NotFoundCategory() throws Exception {
        CategoryRequestDto categoryRequestDto = new CategoryRequestDto("전자기기");
        String requestBody = mapper.writeValueAsString(categoryRequestDto);

        doThrow(new NotFoundException("Not Found Category"))
                .when(categoryService).modifyCategory(any(Long.class), any(CategoryRequestDto.class));

        ResultActions perform = mockMvc.perform(patch("/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFound"))
                .andExpect(jsonPath("$.message").value("Not Found Category"))
                .andExpect(jsonPath("$.path").value("/categories/1"));
    }

    @Test
    @DisplayName("카테고리 삭제 테스트")
    void removeCategoryTest() throws Exception {
        doNothing().when(categoryService).deleteCategory(anyLong());

        ResultActions perform = mockMvc.perform(delete("/categories/1")
                .contentType(MediaType.APPLICATION_JSON));

        perform
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("카테고리 삭제 테스트 - 없는 카테고리 삭제시")
    void removeCategoryTest_NotFoundCategory() throws Exception {
        doThrow(new NotFoundException("Not Found Category"))
                .when(categoryService).deleteCategory(anyLong());

        ResultActions perform = mockMvc.perform(delete("/categories/1"));

        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFound"))
                .andExpect(jsonPath("$.message").value("Not Found Category"))
                .andExpect(jsonPath("$.path").value("/categories/1"));
    }
}