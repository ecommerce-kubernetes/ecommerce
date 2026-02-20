package com.example.userservice.api.user.saga.service;

import com.example.common.user.UserCommandType;
import com.example.common.user.UserSagaCommand;
import com.example.userservice.api.user.saga.domain.model.ProcessedSagaEvent;
import com.example.userservice.api.user.saga.domain.repository.ProcessedSagaEventRepository;
import com.example.userservice.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSagaCommandExecutor {

    private final UserService userService;
    private final ProcessedSagaEventRepository eventRepository;

    public boolean processSagaCommand(UserSagaCommand command) {
        if (eventRepository.existsBySagaIdAndCommandType(command.getSagaId(), command.getType().name())){
            return true;
        }

        if (command.getType() == UserCommandType.USE_POINT) {
            userService.deductPoints(command.getUserId(), command.getUsedPoint());
        } else {
            userService.refundPoints(command.getUserId(), command.getUsedPoint());
        }

        ProcessedSagaEvent event = ProcessedSagaEvent.create(command.getSagaId(), command.getType().name());
        eventRepository.save(event);
        return false;
    }
}
