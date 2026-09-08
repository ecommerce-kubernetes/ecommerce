package com.example.product_service.option.application.service;

import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.common.exception.OptionErrorCode;
import com.example.product_service.common.util.IdGenerator;
import com.example.product_service.option.application.port.OptionProductPort;
import com.example.product_service.option.application.port.OptionTypeRepository;
import com.example.product_service.option.application.service.dto.command.AddOptionValueCommand;
import com.example.product_service.option.application.service.dto.command.CreateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.command.UpdateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.command.UpdateOptionValueCommand;
import com.example.product_service.option.domain.OptionType;
import com.example.product_service.option.domain.OptionValue;
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

    private final OptionTypeRepository optionTypeRepository;

    private final OptionProductPort optionProductPort;

    private final IdGenerator idGenerator;

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
        OptionType optionType = optionTypeRepository.findById(command.optionTypeId())
                .orElseThrow(() -> new BusinessException(OptionErrorCode.OPTION_TYPE_NOT_FOUND));

        if (optionTypeRepository.existsByNameAndIdNot(command.name(), optionType.getId())) {
            throw new BusinessException(OptionErrorCode.OPTION_TYPE_DUPLICATE_NAME);
        }

        optionType.update(command.name());

        return optionType.getId();
    }

    public void deleteOptionType(Long optionTypeId) {
        OptionType optionType = optionTypeRepository.findById(optionTypeId)
                .orElseThrow(() -> new BusinessException(OptionErrorCode.OPTION_TYPE_NOT_FOUND));

        if (optionProductPort.existsProductForOptionType(optionTypeId)) {
            throw new BusinessException(OptionErrorCode.OPTION_TYPE_IN_PRODUCT);
        }

        optionTypeRepository.delete(optionType);
    }

    public Long addOptionValue(AddOptionValueCommand command) {
        OptionType optionType = optionTypeRepository.findById(command.optionTypeId())
                .orElseThrow(() -> new BusinessException(OptionErrorCode.OPTION_TYPE_NOT_FOUND));

        CreateOptionValueContext valueContext = CreateOptionValueContext.of(idGenerator.generate(), command.name());

        OptionValue optionValue = optionType.addOptionValue(valueContext);
        return optionValue.getId();
    }

    public Long updateOptionValue(UpdateOptionValueCommand command) {
        OptionType optionType = optionTypeRepository.findById(command.optionTypeId())
                .orElseThrow(() -> new BusinessException(OptionErrorCode.OPTION_TYPE_NOT_FOUND));

        optionType.updateOptionValue(command.optionValueId(), command.name());

        return command.optionValueId();
    }

    public void deleteOptionValue(Long optionTypeId, Long optionValueId) {
        OptionType optionType = optionTypeRepository.findById(optionTypeId)
                .orElseThrow(() -> new BusinessException(OptionErrorCode.OPTION_TYPE_NOT_FOUND));

        if (optionProductPort.existsProductForOptionValue(optionValueId)) {
            throw new BusinessException(OptionErrorCode.OPTION_VALUE_IN_PRODUCT);
        }

        optionType.deleteOptionValue(optionValueId);
    }

    private CreateOptionTypeContext mapToCreateOptionTypeContext(CreateOptionTypeCommand command) {
        List<CreateOptionValueContext> valueContexts = command.values().stream()
                .map(valueCommand -> CreateOptionValueContext.of(idGenerator.generate(), valueCommand.name())).toList();

        return CreateOptionTypeContext.of(idGenerator.generate(), command.name(), valueContexts);
    }
}
