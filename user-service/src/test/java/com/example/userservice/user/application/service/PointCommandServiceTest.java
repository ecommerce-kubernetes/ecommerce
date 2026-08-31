package com.example.userservice.user.application.service;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.support.annotation.IsolatedTest;
import com.example.userservice.user.application.port.PointHistoryRepository;
import com.example.userservice.user.application.port.UserRepository;
import com.example.userservice.user.domain.PointHistory;
import com.example.userservice.user.domain.PointHistoryFixtureBuilder;
import com.example.userservice.user.domain.User;
import com.example.userservice.user.domain.UserFixtureBuilder;
import com.example.userservice.user.exception.UserErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IsolatedTest
@Transactional
class PointCommandServiceTest {

    @Autowired
    private PointCommandService pointCommandService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Test
    @DisplayName("포인트를 적립하고 적립 내역을 저장한다.")
    void addPoint() {
        //given
        User user = userRepository.save(UserFixtureBuilder.given().build());
        Long referenceId = 1L;
        //when
        pointCommandService.addPoint(user.getId(), referenceId, Money.wons(1000L));
        //then
        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(savedUser.getPoint()).isEqualTo(Money.wons(1000L));
        assertThat(pointHistoryRepository.existsByReferenceId(referenceId)).isTrue();
    }

    @Test
    @DisplayName("이미 처리된 referenceId면 포인트를 적립하지 않는다.")
    void addPoint_whenAlreadyProcessed_thenSkip() {
        //given
        User user = userRepository.save(UserFixtureBuilder.given().build());
        Long referenceId = 1L;
        PointHistory history = PointHistoryFixtureBuilder.given()
                .withReferenceId(referenceId)
                .withUser(user)
                .buildAddHistory();
        pointHistoryRepository.save(history);
        //when
        pointCommandService.addPoint(user.getId(), referenceId, Money.wons(1000L));
        //then
        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(savedUser.getPoint()).isEqualTo(Money.ZERO);
    }

    @Test
    @DisplayName("포인트 적립시 유저를 찾을 수 없으면 예외가 발생한다.")
    void addPoint_whenUserNotFound_thenThrownException() {
        //given
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
        userRepository.save(user);
        Long referenceId = 1L;
        //when
        pointCommandService.deductPoint(user.getId(), referenceId, Money.wons(2000L));
        //then
        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(savedUser.getPoint()).isEqualTo(Money.wons(3000L));
        assertThat(pointHistoryRepository.existsByReferenceId(referenceId)).isTrue();
    }

    @Test
    @DisplayName("이미 처리된 referenceId면 포인트를 차감하지 않는다.")
    void deductPoint_whenAlreadyProcessed_thenSkip() {
        //given
        User user = UserFixtureBuilder.given().build();
        user.addPoints(Money.wons(5000L));
        userRepository.save(user);
        Long referenceId = 1L;
        PointHistory history = PointHistoryFixtureBuilder.given()
                .withReferenceId(referenceId)
                .withUser(user)
                .buildDeductHistory();
        pointHistoryRepository.save(history);
        //when
        pointCommandService.deductPoint(user.getId(), referenceId, Money.wons(2000L));
        //then
        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(savedUser.getPoint()).isEqualTo(Money.wons(5000L));
    }

    @Test
    @DisplayName("보유 포인트보다 많은 포인트를 차감하면 예외가 발생한다.")
    void deductPoint_whenInsufficientPoint_thenThrownException() {
        //given
        User user = userRepository.save(UserFixtureBuilder.given().build());
        //when
        //then
        assertThatThrownBy(() -> pointCommandService.deductPoint(user.getId(), 1L, Money.wons(1000L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.INSUFFICIENT_POINTS);
    }

    @Test
    @DisplayName("포인트 차감시 유저를 찾을 수 없으면 예외가 발생한다.")
    void deductPoint_whenUserNotFound_thenThrownException() {
        //given
        //when
        //then
        assertThatThrownBy(() -> pointCommandService.deductPoint(999L, 1L, Money.wons(1000L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }
}
