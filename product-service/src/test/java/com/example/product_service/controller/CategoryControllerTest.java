package com.example.product_service.controller;

import com.example.product_service.controller.util.SortFieldValidator;
import com.example.product_service.dto.request.CategoryRequestDto;
import com.example.product_service.dto.request.ModifyCategoryRequestDto;
import com.example.product_service.dto.response.CategoryResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CategoryController.class)
@Import(SortFieldValidator.class)
class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    CategoryService categoryService;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Category 생성 테스트")
    void createCategoryTest() throws Exception {
        CategoryRequestDto categoryRequestDto = new CategoryRequestDto("식품",null, "http://test.jpg");
        CategoryResponseDto categoryResponseDto = new CategoryResponseDto(1L, "식품", null, "http://test.jpg");

        when(categoryService.saveCategory(any(CategoryRequestDto.class))).thenReturn(categoryResponseDto);

        String requestBody = mapper.writeValueAsString(categoryRequestDto);
        ResultActions perform = mockMvc.perform(post("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        perform
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(categoryRequestDto.getName()))
                .andExpect(jsonPath("$.parentId").value(categoryRequestDto.getParentId()));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCategoryRequests")
    @DisplayName("Category 생성 테스트 - 입력값 검증 테스트")
    void createCategoryTest_InvalidCategoryRequestDto(CategoryRequestDto requestDto,
                                                      String expectedMessage,
                                                      String expectedFieldName,
                                                      String expectedErrorMessage) throws Exception {

        String requestBody = mapper.writeValueAsString(requestDto);
        ResultActions perform = mockMvc.perform(post("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BadRequest"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.errors[*].fieldName").value(expectedFieldName))
                .andExpect(jsonPath("$.errors[*].message").value(expectedErrorMessage))
                .andExpect(jsonPath("$.path").value("/categories"));
    }

    @Test
    @DisplayName("Category 변경 테스트")
    void updateCategoryTest() throws Exception {
        ModifyCategoryRequestDto categoryRequestDto = new ModifyCategoryRequestDto("노트북", 2L, "http://test.jpg");
        CategoryResponseDto categoryResponseDto = new CategoryResponseDto(1L, "노트북", 2L, "http://test.jpg");
        String requestBody = mapper.writeValueAsString(categoryRequestDto);

        when(categoryService.modifyCategory(any(Long.class), any(ModifyCategoryRequestDto.class))).thenReturn(categoryResponseDto);

        ResultActions perform = mockMvc.perform(patch("/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryResponseDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryRequestDto.getName()))
                .andExpect(jsonPath("$.parentId").value(categoryRequestDto.getParentId()))
                .andExpect(jsonPath("$.iconUrl").value(categoryRequestDto.getIconUrl()));
    }

    @Test
    @DisplayName("Category 변경 테스트 - 입력값 검증 테스트")
    void updateCategoryTest_InvalidCategoryRequestDto() throws Exception {

        ModifyCategoryRequestDto modifyCategoryRequestDto = new ModifyCategoryRequestDto(null, null, "text1");

        String requestBody = mapper.writeValueAsString(modifyCategoryRequestDto);

        ResultActions perform = mockMvc.perform(patch("/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BadRequest"))
                .andExpect(jsonPath("$.message").value("Validation Error"))
                .andExpect(jsonPath("$.errors[*].fieldName").value("iconUrl"))
                .andExpect(jsonPath("$.errors[*].message").value("Invalid ImgUrl"))
                .andExpect(jsonPath("$.path").value("/categories/1"));
    }

    @Test
    @DisplayName("Category 변경 테스트 - parentId 와 변경 하는 CategoryId 가 같은경우")
    void updateCategoryTest_categoryIdEqualToParentId() throws Exception {
        CategoryRequestDto categoryRequestDto = new CategoryRequestDto("노트북", 1L, "http://test.jpg");
        String requestBody = mapper.writeValueAsString(categoryRequestDto);

        ResultActions perform = mockMvc.perform(patch("/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BadRequest"))
                .andExpect(jsonPath("$.message").value("An item cannot be set as its own parent"))
                .andExpect(jsonPath("$.path").value("/categories/1"));
    }

    @Test
    @DisplayName("Category 수정 테스트 - 카테고리를 찾을 수 없는 경우")
    void updateCategoryNameTest_NotFoundCategory() throws Exception {
        ModifyCategoryRequestDto categoryRequestDto = new ModifyCategoryRequestDto("전자기기", null, "http://test.jpg");
        String requestBody = mapper.writeValueAsString(categoryRequestDto);

        doThrow(new NotFoundException("Not Found Category"))
                .when(categoryService).modifyCategory(any(Long.class), any(ModifyCategoryRequestDto.class));

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

    @Test
    @DisplayName("카테고리 단일 조회 테스트")
    void getCategoryByIdTest() throws Exception {
        CategoryResponseDto categoryResponseDto = new CategoryResponseDto(1L, "식품", 2L, null);

        when(categoryService.getCategoryDetails(anyLong())).thenReturn(categoryResponseDto);

        ResultActions perform = mockMvc.perform(get("/categories/1"));

        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryResponseDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryResponseDto.getName()))
                .andExpect(jsonPath("$.parentId").value(categoryResponseDto.getParentId()))
                .andExpect(jsonPath("$.iconUrl").value(categoryResponseDto.getIconUrl()));
    }

    @Test
    @DisplayName("카테고리 조회 테스트 - 카테고리가 없을때")
    void getCategoryByIdTest_NotFoundCategory() throws Exception {
        doThrow(new NotFoundException("Not Found Category")).when(categoryService).getCategoryDetails(anyLong());

        ResultActions perform = mockMvc.perform(get("/categories/1"));

        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFound"))
                .andExpect(jsonPath("$.message").value("Not Found Category"))
                .andExpect(jsonPath("$.path").value("/categories/1"));
    }

    @Test
    @DisplayName("대표 카테고리 리스트 조회 테스트")
    void getMainCategoryListTest() throws Exception {
        List<CategoryResponseDto> content = new ArrayList<>();
        content.add(new CategoryResponseDto(1L, "식품", null, "http://test1.jpg"));
        content.add(new CategoryResponseDto(2L, "전자기기", null,"http://test2.jpg"));
        content.add(new CategoryResponseDto(3L, "의류", null,"http://test3.jpg"));
        content.add(new CategoryResponseDto(4L, "가구", null,"http://test4.jpg"));

        PageDto<CategoryResponseDto> pageDto = new PageDto<>(
                content,
                0,
                1,
                10,
                4
        );

        when(categoryService.getRootCategories(any(Pageable.class))).thenReturn(pageDto);

        ResultActions perform = mockMvc.perform(get("/categories")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id")
                .param("direction", "asc")
        );

        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0));
    }

    @Test
    @DisplayName("자식 카테고리 리스트 조회")
    void getChildByCategoryIdTest() throws Exception {
        List<CategoryResponseDto> responseList = new ArrayList<>();
        responseList.add(new CategoryResponseDto(5L, "반찬류", 1L, "http://test1.jpg"));
        responseList.add(new CategoryResponseDto(6L, "냉장", 2L, "http://test2.jpg"));
        responseList.add(new CategoryResponseDto(6L, "냉동", 3L, "http://test3.jpg"));

        when(categoryService.getChildCategories(anyLong())).thenReturn(responseList);

        ResultActions perform = mockMvc.perform(get("/categories/1/child"));

        perform
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("특정 카테고리의 루트 조회")
    void getRootByCategoryIdTest() throws Exception {
        CategoryResponseDto categoryResponseDto = new CategoryResponseDto(1L, "식품", null, "http://test.jpg");

        when(categoryService.getRootCategoryDetailsOf(anyLong())).thenReturn(categoryResponseDto);

        ResultActions perform = mockMvc.perform(get("/categories/6/root"));

        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryResponseDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryResponseDto.getName()))
                .andExpect(jsonPath("$.parentId").value(categoryResponseDto.getParentId()))
                .andExpect(jsonPath("$.iconUrl").value(categoryResponseDto.getIconUrl()));
    }

    @Test
    @DisplayName("특정 카테고리 루트 조회 _ 카테고리 NotFound")
    void getRootByCategoryIdTest_NotFound() throws Exception {
        doThrow(new NotFoundException("Not Found Category")).when(categoryService).getRootCategoryDetailsOf(anyLong());

        ResultActions perform = mockMvc.perform(get("/categories/999/root"));

        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFound"))
                .andExpect(jsonPath("$.message").value("Not Found Category"))
                .andExpect(jsonPath("$.path").value("/categories/999/root"));
    }

    private static Stream<Arguments> provideInvalidCategoryRequests(){
        return Stream.of(
                Arguments.of(
                        new CategoryRequestDto(),
                        "Validation Error",
                        "name",
                        "Category name is required"
                ),
                Arguments.of(
                        new CategoryRequestDto("Category", null, "test.jpg"),
                        "Validation Error",
                        "iconUrl",
                        "Invalid ImgUrl"
                )
        );
    }

}