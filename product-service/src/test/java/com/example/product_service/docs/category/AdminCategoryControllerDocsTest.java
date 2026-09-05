package com.example.product_service.docs.category;

import com.example.product_service.category.adapter.in.web.AdminCategoryController;
import com.example.product_service.category.adapter.in.web.dto.request.CreateCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.MoveCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.UpdateCategoryRequest;
import com.example.product_service.category.application.service.CategoryCommandService;
import com.example.product_service.category.application.service.dto.command.CreateCategoryCommand;
import com.example.product_service.category.application.service.dto.command.UpdateCategoryCommand;
import com.example.product_service.docs.RestDocsSupport;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.example.product_service.category.fixture.CategoryRequestFixture.*;
import static com.example.product_service.docs.descriptor.CategoryDescriptor.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class AdminCategoryControllerDocsTest extends RestDocsSupport {

    private CategoryCommandService categoryCommandService = Mockito.mock(CategoryCommandService.class);

    @Override
    protected Object initController() {
        return new AdminCategoryController(categoryCommandService);
    }

    @Test
    @DisplayName("카테고리를 생성한다")
    void saveCategory() throws Exception {
        //given
        CreateCategoryRequest request = anCreateCategoryRequest().build();
        Long categoryId = 1L;

        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");

        given(categoryCommandService.saveCategory(any(CreateCategoryCommand.class)))
                .willReturn(categoryId);
        //when
        //then
        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document(
                        "admin/categories/create",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestFields(createRequest()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(createResponse())
                ));
    }

    @Test
    @DisplayName("카테고리를 수정한다")
    void updateCategory() throws Exception {
        //given
        UpdateCategoryRequest request = anUpdateCategoryRequest().build();

        Long categoryId = 1L;
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(categoryCommandService.updateCategory(any(UpdateCategoryCommand.class)))
                .willReturn(categoryId);
        //when
        //then
        mockMvc.perform(patch("/admin/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "admin/categories/update",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("categoryId").description("수정할 카테고리 ID")),
                        requestHeaders(AUTH_HEADER),
                        requestFields(updateRequest()),
                        responseFields(updateResponse())
                ));
    }

    @Test
    @DisplayName("카테고리의 부모를 변경한다")
    void moveParent() throws Exception {
        //given
        MoveCategoryRequest request = anMoveCategoryRequest().build();

        Long categoryId = 1L;

        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(categoryCommandService.moveParent(anyLong(), anyLong())).willReturn(categoryId);
        //when
        //then
        mockMvc.perform(patch("/admin/categories/{categoryId}/move", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "admin/categories/move",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        pathParameters(parameterWithName("categoryId").description("수정할 카테고리 ID")),
                        requestFields(moveRequest()),
                        responseFields(moveResponse())
                ));
    }

    @Test
    @DisplayName("카테고리를 삭제한다")
    void deleteCategory() throws Exception {
        //given
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        willDoNothing().given(categoryCommandService).deleteCategory(anyLong());
        //when
        //then
        mockMvc.perform(delete("/admin/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document(
                        "admin/categories/delete",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        pathParameters(parameterWithName("categoryId").description("삭제할 카테고리 ID"))
                ));
    }
}
