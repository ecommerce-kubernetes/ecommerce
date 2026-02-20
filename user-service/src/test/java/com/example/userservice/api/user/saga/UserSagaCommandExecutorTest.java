package com.example.userservice.api.user.saga;

import com.example.common.user.UserCommandType;
import com.example.common.user.UserSagaCommand;
import com.example.userservice.api.user.saga.domain.model.ProcessedSagaEvent;
import com.example.userservice.api.user.saga.domain.repository.ProcessedSagaEventRepository;
import com.example.userservice.api.user.saga.service.UserSagaCommandExecutor;
import com.example.userservice.api.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserSagaCommandExecutorTest {

    @InjectMocks
    private UserSagaCommandExecutor executor;

    @Mock
    private UserService userService;
    @Mock
    private ProcessedSagaEventRepository repository;

    @Nested
    @DisplayName("유저 포인트 차감 사가 처리")
    class ProcessSagaCommand {

        @Test
        @DisplayName("포인트 차감과 처리 내역을 저장한다")
        void processSagaCommand() {
            //given
            UserSagaCommand command = UserSagaCommand
                    .of(UserCommandType.USE_POINT, 1L, "ORDER_NO", 1L, 1000L, LocalDateTime.now());
            given(repository.existsBySagaIdAndCommandType(command.getSagaId(), command.getType().name()))
                    .willReturn(false);
            //when
            boolean isProcessed = executor.processSagaCommand(command);
            //then
            assertThat(isProcessed).isFalse();
            verify(userService, times(1)).deductPoints(command.getUserId(), command.getUsedPoint());
            verify(repository, times(1)).save(any(ProcessedSagaEvent.class));
        }
        
        @Test
        @DisplayName("중복 요청시 포인트 차감과 처리 내역 저장은 건너뛴다")
        void processSagaCommand_duplicate_skip() {
            //given
            UserSagaCommand command = UserSagaCommand
                    .of(UserCommandType.USE_POINT, 1L, "ORDER_NO", 1L, 1000L, LocalDateTime.now());
            given(repository.existsBySagaIdAndCommandType(command.getSagaId(), command.getType().name()))
                    .willReturn(true);
            //when
            boolean isProcessed = executor.processSagaCommand(command);
            //then
            assertThat(isProcessed).isTrue();
            verify(userService, never()).deductPoints(anyLong(), anyLong());
            verify(repository, never()).save(any());
        }
    }
}
