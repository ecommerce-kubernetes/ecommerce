package com.example.userservice.saga.service;

import com.example.common.user.UserCommandType;
import com.example.common.user.UserSagaCommand;
import com.example.userservice.saga.domain.model.ProcessedSagaEvent;
import com.example.userservice.saga.domain.repository.ProcessedSagaEventRepository;
import com.example.userservice.user.application.service.UserCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSagaCommandExecutor {

    private final UserCommandService userCommandService;
    private final ProcessedSagaEventRepository eventRepository;

    public boolean processSagaCommand(UserSagaCommand command) {
        if (eventRepository.existsBySagaIdAndCommandType(command.getSagaId(), command.getType().name())){
            return true;
        }

        if (command.getType() == UserCommandType.USE_POINT) {
            userCommandService.deductPoints(command.getUserId(), command.getUsedPoint());
        } else {
            userCommandService.refundPoints(command.getUserId(), command.getUsedPoint());
        }

        ProcessedSagaEvent event = ProcessedSagaEvent.create(command.getSagaId(), command.getType().name());
        eventRepository.save(event);
        return false;
    }
}
