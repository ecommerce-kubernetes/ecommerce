package com.example.userservice.user.application.service;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.user.application.port.PointHistoryRepository;
import com.example.userservice.user.application.port.UserRepository;
import com.example.userservice.user.domain.PointHistory;
import com.example.userservice.user.domain.User;
import com.example.userservice.user.domain.UserFixtureBuilder;
import com.example.userservice.user.domain.vo.PointCommandType;
import com.example.userservice.user.exception.UserErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PointCommandServiceTest {

    @InjectMocks
    private PointCommandService pointCommandService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private IdGenerator idGenerator;

    @Captor
    private ArgumentCaptor<PointHistory> pointHistoryCaptor;

    @Test
    @DisplayName("포인트를 적립하고 적립 내역을 저장한다.")
    void addPoint() {
        //given
        User user = UserFixtureBuilder.given().build();
        Long referenceId = 1L;
        given(pointHistoryRepository.existsByReferenceId(referenceId)).willReturn(false);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(idGenerator.generate()).willReturn(999L);
        //when
        pointCommandService.addPoint(user.getId(), referenceId, Money.wons(1000L));
        //then
        assertThat(user.getPoint()).isEqualTo(Money.wons(1000L));

        then(pointHistoryRepository).should().save(pointHistoryCaptor.capture());
        PointHistory savedHistory = pointHistoryCaptor.getValue();
        assertThat(savedHistory.getReferenceId()).isEqualTo(referenceId);
        assertThat(savedHistory.getUser()).isEqualTo(user);
        assertThat(savedHistory.getAmount()).isEqualTo(Money.wons(1000L));
        assertThat(savedHistory.getType()).isEqualTo(PointCommandType.ADD);
    }

    @Test
    @DisplayName("이미 처리된 referenceId면 포인트를 적립하지 않는다.")
    void addPoint_whenAlreadyProcessed_thenSkip() {
        //given
        Long referenceId = 1L;
        given(pointHistoryRepository.existsByReferenceId(referenceId)).willReturn(true);
        //when
        pointCommandService.addPoint(1L, referenceId, Money.wons(1000L));
        //then
        then(userRepository).should(never()).findById(anyLong());
        then(pointHistoryRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("포인트 적립시 유저를 찾을 수 없으면 예외가 발생한다.")
    void addPoint_whenUserNotFound_thenThrownException() {
        //given
        given(pointHistoryRepository.existsByReferenceId(1L)).willReturn(false);
        given(userRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> pointCommandService.addPoint(999L, 1L, Money.wons(1000L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("포인트를 차감하고 차감 내역을 저장한다.")
    void deductPoint() {
        //given
        User user = UserFixtureBuilder.given().build();
        user.addPoints(Money.wons(5000L));
        Long referenceId = 1L;
        given(pointHistoryRepository.existsByReferenceId(referenceId)).willReturn(false);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(idGenerator.generate()).willReturn(999L);
        //when
        pointCommandService.deductPoint(user.getId(), referenceId, Money.wons(2000L));
        //then
        assertThat(user.getPoint()).isEqualTo(Money.wons(3000L));

        then(pointHistoryRepository).should().save(pointHistoryCaptor.capture());
        PointHistory savedHistory = pointHistoryCaptor.getValue();
        assertThat(savedHistory.getType()).isEqualTo(PointCommandType.DEDUCT);
        assertThat(savedHistory.getAmount()).isEqualTo(Money.wons(2000L));
    }

    @Test
    @DisplayName("이미 처리된 referenceId면 포인트를 차감하지 않는다.")
    void deductPoint_whenAlreadyProcessed_thenSkip() {
        //given
        Long referenceId = 1L;
        given(pointHistoryRepository.existsByReferenceId(referenceId)).willReturn(true);
        //when
        pointCommandService.deductPoint(1L, referenceId, Money.wons(2000L));
        //then
        then(userRepository).should(never()).findById(anyLong());
        then(pointHistoryRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("보유 포인트보다 많은 포인트를 차감하면 예외가 발생한다.")
    void deductPoint_whenInsufficientPoint_thenThrownException() {
        //given
        User user = UserFixtureBuilder.given().build();
        given(pointHistoryRepository.existsByReferenceId(1L)).willReturn(false);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        //when
        //then
        assertThatThrownBy(() -> pointCommandService.deductPoint(user.getId(), 1L, Money.wons(1000L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.INSUFFICIENT_POINTS);

        then(pointHistoryRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("포인트 차감시 유저를 찾을 수 없으면 예외가 발생한다.")
    void deductPoint_whenUserNotFound_thenThrownException() {
        //given
        given(pointHistoryRepository.existsByReferenceId(1L)).willReturn(false);
        given(userRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> pointCommandService.deductPoint(999L, 1L, Money.wons(1000L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

}
