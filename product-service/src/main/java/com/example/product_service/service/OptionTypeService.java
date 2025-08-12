package com.example.product_service.service;

import com.example.product_service.common.MessagePath;
import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionTypeResponse;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.OptionTypeRepository;
import com.example.product_service.repository.OptionValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.example.product_service.common.MessagePath.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class OptionTypeService {

    private final OptionTypeRepository optionTypeRepository;
    private final MessageSourceUtil ms;

    @Transactional
    public OptionTypeResponse saveOptionType(OptionTypeRequest request) {
        checkConflictTypeName(request.getName());
        OptionTypes save = optionTypeRepository.save(new OptionTypes(request.getName()));
        return new OptionTypeResponse(save);
    }

    @Transactional
    public OptionValueResponse saveOptionValue(Long optionTypeId, OptionValueRequest request) {
        OptionTypes optionType = findByIdOrThrow(optionTypeId);
        checkConflictValueName(optionType, request.getValue());
        OptionValues optionValue = new OptionValues(request.getValue());
        optionType.addOptionValue(optionValue);
        return new OptionValueResponse(optionValue);
    }

    public List<OptionTypeResponse> getOptionTypes() {
        return null;
    }


    @Transactional
    public OptionTypeResponse updateOptionTypeById(Long optionTypeId, OptionTypeRequest requestDto) {
        OptionTypes target = optionTypeRepository.findById(optionTypeId)
                .orElseThrow(() -> new NotFoundException("Not Found OptionType"));

        target.setName(requestDto.getName());
        try{
            optionTypeRepository.flush();
        } catch (DataIntegrityViolationException ex){
            throw new DuplicateResourceException("OptionTypes name Conflict");
        }
        return new OptionTypeResponse(target);
    }


    @Transactional
    public void deleteOptionTypeById(Long optionTypeId) {
        OptionTypes target = optionTypeRepository.findById(optionTypeId)
                .orElseThrow(() -> new NotFoundException("Not Found OptionType"));

        optionTypeRepository.delete(target);
    }


    public List<OptionValueResponse> getOptionValuesByTypeId(Long optionTypeId) {
        OptionTypes optionTypes = optionTypeRepository.findByIdWithOptionValues(optionTypeId)
                .orElseThrow(() -> new NotFoundException("Not Found OptionTypes"));

        List<OptionValues> optionValues = optionTypes.getOptionValues();

        return null;
    }

    private void checkConflictTypeName(String name) {
        if(optionTypeRepository.existsByName(name)){
            throw new DuplicateResourceException(ms.getMessage(OPTION_TYPE_CONFLICT));
        }
    }

    private void checkConflictValueName(OptionTypes optionType, String name){
        boolean isConflict = optionType.getOptionValues().stream()
                .anyMatch(v -> v.getValue().equals(name));
        if(isConflict){
            throw new DuplicateResourceException(ms.getMessage(OPTION_VALUE_CONFLICT));
        }
    }

    private OptionTypes findByIdOrThrow(Long optionTypeId){
        return optionTypeRepository.findById(optionTypeId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(OPTION_TYPE_NOT_FOUND)));
    }
}
