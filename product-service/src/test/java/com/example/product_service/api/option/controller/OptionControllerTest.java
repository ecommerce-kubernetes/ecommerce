package com.example.product_service.api.option.controller;

import com.example.product_service.api.common.security.model.UserRole;
import com.example.product_service.api.option.controller.dto.OptionRequest;
import com.example.product_service.api.option.service.dto.OptionResponse;
import com.example.product_service.api.option.service.dto.OptionValueResponse;
import com.example.product_service.api.support.ControllerTestSupport;
import com.example.product_service.api.support.security.annotation.WithCustomMockUser;
import com.example.product_service.api.support.security.config.TestSecurityConfig;
import com.example.product_service.config.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({TestConfig.class, TestSecurityConfig.class})
public class OptionControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("옵션을 저장한다")
    @WithCustomMockUser
    void saveOption() throws Exception {
        //given
        OptionRequest request = createOptionRequest().build();
        OptionResponse response = createOptionResponse().build();
        given(optionService.saveOption(anyString(), anyList()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/options")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("옵션을 저장하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void saveOptionWithUserRole() throws Exception {
        //given
        OptionRequest request = createOptionRequest().build();
        //when
        //then
        mockMvc.perform(post("/options")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 없습니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options"));
    }

    @Test
    @DisplayName("로그인 하지 않은 유저는 옵션을 저장할 수 없다")
    void saveOption_unAuthentication() throws Exception {
        //given
        OptionRequest request = createOptionRequest().build();
        //when
        //then
        mockMvc.perform(post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 없습니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options"));
    }

    @Test
    @DisplayName("옵션을 저장할때 이름은 필수이다")
    @WithCustomMockUser
    void saveOption_validation() throws Exception {
        //given
        OptionRequest request = createOptionRequest().name(null).build();
        //when
        //then
        mockMvc.perform(post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("code").value("VALIDATION"))
                .andExpect(jsonPath("message").value("옵션 이름은 필수 입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options"));
    }

    @Test
    @DisplayName("옵션을 조회한다")
    void getOption() throws Exception {
        //given
        OptionResponse response = createOptionResponse().build();
        given(optionService.getOption(anyLong()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/options/{optionId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("옵션 목록을 조회한다")
    void getOptions() throws Exception {
        //given
        OptionResponse response = createOptionResponse().build();
        given(optionService.getOptions())
                .willReturn(List.of(response));
        //when
        //then
        mockMvc.perform(get("/options")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(response))));
    }

    @Test
    @DisplayName("옵션을 수정한다")
    @WithCustomMockUser
    void updateOption() throws Exception {
        //given
        OptionRequest request = createOptionRequest().build();
        OptionResponse response = createOptionResponse().build();
        given(optionService.updateOption(anyLong(), anyString(), anyList()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(put("/options/{optionId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("옵션을 수정하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void updateOptionWithUserRole() throws Exception {
        //given
        OptionRequest request = createOptionRequest().build();
        //when
        //then
        mockMvc.perform(put("/options/{optionId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 없습니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options/1"));
    }

    @Test
    @DisplayName("로그인 하지 않은 회원은 옵션을 수정할 수 없다")
    void updateOption_unAuthentication() throws Exception {
        //given
        OptionRequest request = createOptionRequest().build();
        //when
        //then
        mockMvc.perform(put("/options/{optionId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 없습니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options/1"));
    }

    @Test
    @DisplayName("옵션을 삭제한다")
    @WithCustomMockUser
    void deleteOption() throws Exception {
        //given
        willDoNothing().given(optionService).deleteOption(anyLong());
        //when
        //then
        mockMvc.perform(delete("/options/{optionId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("옵션을 삭제하려면 유저 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void deleteOptionWithUserRole() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/options/{optionId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 없습니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options/1"));
    }

    @Test
    @DisplayName("로그인 하지 않은 회원은 옵션을 삭제할 수 없다")
    void deleteOption_unAuthentication() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/options/{optionId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 없습니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options/1"));
    }

    private OptionRequest.OptionRequestBuilder createOptionRequest() {
        return OptionRequest.builder().name("사이즈").values(
                List.of("XL", "L", "M", "S")
        );
    }

    private OptionResponse.OptionResponseBuilder createOptionResponse() {
        return OptionResponse.builder()
                .id(1L)
                .name("사이즈")
                .values(
                        List.of(
                                OptionValueResponse.builder().id(1L).value("XL").build(),
                                OptionValueResponse.builder().id(2L).value("L").build(),
                                OptionValueResponse.builder().id(3L).value("M").build(),
                                OptionValueResponse.builder().id(4L).value("S").build()
                        ));
    }
}
