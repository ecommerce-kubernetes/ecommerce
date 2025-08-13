package com.example.product_service.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.product_service.common.MessagePath.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class OptionTypeService {

    private final OptionTypeRepository optionTypeRepository;
    private final OptionValueRepository optionValueRepository;
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
        checkConflictValueName(optionType, request.getValueName());
        OptionValues optionValue = new OptionValues(request.getValueName());
        optionType.addOptionValue(optionValue);
        OptionValues saved = optionValueRepository.save(optionValue);
        return new OptionValueResponse(saved);
    }

    public List<OptionTypeResponse> getOptionTypes() {
        List<OptionTypes> typesList = optionTypeRepository.findAll();
        return typesList.stream().map(OptionTypeResponse::new).toList();
    }

    public List<OptionValueResponse> getOptionValuesByTypeId(Long optionTypeId) {
        OptionTypes type = findByIdOrThrow(optionTypeId);
        return type.getOptionValues().stream().map(OptionValueResponse::new).toList();
    }

    @Transactional
    public OptionTypeResponse updateOptionTypeById(Long optionTypeId, OptionTypeRequest request) {
        OptionTypes target = findByIdOrThrow(optionTypeId);
        checkConflictTypeName(request.getName());
        target.setName(request.getName());
        return new OptionTypeResponse(target);
    }


    @Transactional
    public void deleteOptionTypeById(Long optionTypeId) {
        OptionTypes target = findByIdOrThrow(optionTypeId);
        optionTypeRepository.delete(target);
    }


    private void checkConflictTypeName(String name) {
        if(optionTypeRepository.existsByName(name)){
            throw new DuplicateResourceException(ms.getMessage(OPTION_TYPE_CONFLICT));
        }
    }

    private void checkConflictValueName(OptionTypes optionType, String name){
        boolean isConflict = optionType.getOptionValues().stream()
                .anyMatch(v -> v.getValueName().equals(name));
        if(isConflict){
            throw new DuplicateResourceException(ms.getMessage(OPTION_VALUE_CONFLICT));
        }
    }

    private OptionTypes findByIdOrThrow(Long optionTypeId){
        return optionTypeRepository.findById(optionTypeId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(OPTION_TYPE_NOT_FOUND)));
    }
}
