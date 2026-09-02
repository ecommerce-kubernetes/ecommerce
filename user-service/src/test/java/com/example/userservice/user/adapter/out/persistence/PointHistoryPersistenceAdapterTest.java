package com.example.userservice.user.adapter.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PointHistoryPersistenceAdapterTest {

    @InjectMocks
    private PointHistoryPersistenceAdapter pointHistoryPersistenceAdapter;

    @Mock
    private PointHistoryJpaRepository pointHistoryJpaRepository;

    @Test
    @DisplayName("referenceId로 포인트 기록 존재 여부 조회를 위임한다.")
    void existsByReferenceId() {
        //given
        given(pointHistoryJpaRepository.existsByReferenceId(1L)).willReturn(true);
        //when
        boolean exists = pointHistoryPersistenceAdapter.existsByReferenceId(1L);
        //then
        assertThat(exists).isTrue();
        then(pointHistoryJpaRepository).should().existsByReferenceId(1L);
    }
}
