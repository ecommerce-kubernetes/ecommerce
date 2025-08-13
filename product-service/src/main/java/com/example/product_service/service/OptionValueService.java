package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.OptionValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.product_service.common.MessagePath.OPTION_VALUE_CONFLICT;
import static com.example.product_service.common.MessagePath.OPTION_VALUE_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OptionValueService {

    private final OptionValueRepository optionValueRepository;
    private final MessageSourceUtil ms;

    public OptionValueResponse getOptionValueById(Long optionValueId) {
        OptionValues optionValue = findByIdOrThrow(optionValueId);
        return new OptionValueResponse(optionValue);
    }

    @Transactional
    public OptionValueResponse updateOptionValueById(Long optionValueId, OptionValueRequest request) {
        OptionValues target = findByIdOrThrow(optionValueId);
        checkConflictValueName(target.getOptionType(), request.getValueName());
        target.setValueName(request.getValueName());
        return new OptionValueResponse(target);
    }

    public void deleteOptionValueById(Long optionValueId) {

    }

    private OptionValues findByIdOrThrow(Long optionValueId) {
        return optionValueRepository.findById(optionValueId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(OPTION_VALUE_NOT_FOUND)));
    }

    private void checkConflictValueName(OptionTypes optionType, String name){
        boolean isConflict = optionType.getOptionValues().stream()
                .anyMatch(v -> v.getValueName().equals(name));
        if(isConflict){
            throw new DuplicateResourceException(ms.getMessage(OPTION_VALUE_CONFLICT));
        }
    }
}
