package com.example.product_service.controller;

import com.example.product_service.dto.request.ReviewRequestDto;
import com.example.product_service.dto.response.ReviewResponseDto;
import com.example.product_service.service.ReviewService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    ReviewService reviewService;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("리뷰 등록 테스트")
    void registerReviewTest() throws Exception {
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(1L, 5, "맛있습니다", null);
        ReviewResponseDto reviewResponseDto = new ReviewResponseDto(1L,
                5L,
                1L,
                reviewRequestDto.getRating(),
                reviewRequestDto.getContent());

        String content = mapper.writeValueAsString(reviewRequestDto);
        when(reviewService.saveReview(anyLong(), anyLong(), any(ReviewRequestDto.class)))
                .thenReturn(reviewResponseDto);

        ResultActions perform = mockMvc.perform(post("/reviews/5")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", "1")
                .content(content));

        perform
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reviewResponseDto.getId()))
                .andExpect(jsonPath("$.userId").value(reviewResponseDto.getUserId()))
                .andExpect(jsonPath("$.rating").value(reviewResponseDto.getRating()))
                .andExpect(jsonPath("$.content").value(reviewResponseDto.getContent()));
    }

}
