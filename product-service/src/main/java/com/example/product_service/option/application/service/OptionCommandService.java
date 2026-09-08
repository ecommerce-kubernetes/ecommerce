package com.example.product_service.option.application.service;

import com.example.product_service.option.application.port.OptionTypeRepository;
import com.example.product_service.option.application.service.dto.command.AddOptionValueCommand;
import com.example.product_service.option.application.service.dto.command.CreateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.command.UpdateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.command.UpdateOptionValueCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OptionCommandService {

    private OptionTypeRepository optionTypeRepository;

    public Long createOptionType(CreateOptionTypeCommand command) {
        return null;
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
}
