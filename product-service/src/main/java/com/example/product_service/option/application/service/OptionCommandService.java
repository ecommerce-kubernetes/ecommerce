package com.example.product_service.option.application.service;

import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.common.exception.OptionErrorCode;
import com.example.product_service.common.util.IdGenerator;
import com.example.product_service.option.application.port.OptionTypeRepository;
import com.example.product_service.option.application.service.dto.command.AddOptionValueCommand;
import com.example.product_service.option.application.service.dto.command.CreateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.command.UpdateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.command.UpdateOptionValueCommand;
import com.example.product_service.option.domain.OptionType;
import com.example.product_service.option.domain.context.CreateOptionTypeContext;
import com.example.product_service.option.domain.context.CreateOptionValueContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OptionCommandService {

    private OptionTypeRepository optionTypeRepository;

    private IdGenerator idGenerator;

    public Long createOptionType(CreateOptionTypeCommand command) {
        if (optionTypeRepository.existsByName(command.name())) {
            throw new BusinessException(OptionErrorCode.OPTION_TYPE_DUPLICATE_NAME);
        }

        CreateOptionTypeContext context = mapToCreateOptionTypeContext(command);
        OptionType optionType = OptionType.create(context);
        OptionType save = optionTypeRepository.save(optionType);
        return save.getId();
    }

    public Long updateOptionType(UpdateOptionTypeCommand command) {
        return null;
    }

    public void deleteOptionType(Long optionTypeId) {

    }

    public Long addOptionValue(AddOptionValueCommand command) {
        return null;
    }

    public Long updateOptionValue(UpdateOptionValueCommand command) {
        return null;
    }

    public void deleteOptionValue(Long optionTypeId, Long optionValueId) {

    }

    private CreateOptionTypeContext mapToCreateOptionTypeContext(CreateOptionTypeCommand command) {
        List<CreateOptionValueContext> valueContexts = command.values().stream()
                .map(valueCommand -> CreateOptionValueContext.of(idGenerator.generate(), valueCommand.name())).toList();

        return CreateOptionTypeContext.of(idGenerator.generate(), command.name(), valueContexts);
    }
}
