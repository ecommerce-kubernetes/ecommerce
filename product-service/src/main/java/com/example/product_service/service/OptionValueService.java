package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.entity.OptionType;
import com.example.product_service.entity.OptionValue;
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
        OptionValue optionValue = findByIdOrThrow(optionValueId);
        return new OptionValueResponse(optionValue);
    }

    @Transactional
    public OptionValueResponse updateOptionValueById(Long optionValueId, OptionValueRequest request) {
        OptionValue target = findByIdOrThrow(optionValueId);
        checkConflictValueName(target.getOptionType(), request.getValueName());
        target.setOptionValue(request.getValueName());
        return new OptionValueResponse(target);
    }

    @Transactional
    public void deleteOptionValueById(Long optionValueId) {
        OptionValue target = findByIdOrThrow(optionValueId);
        OptionType optionType = target.getOptionType();

        optionType.removeOptionValue(target);
    }

    private OptionValue findByIdOrThrow(Long optionValueId) {
        return optionValueRepository.findById(optionValueId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(OPTION_VALUE_NOT_FOUND)));
    }

    private void checkConflictValueName(OptionType optionType, String name){
        boolean isConflict = optionType.getOptionValues().stream()
                .anyMatch(v -> v.getOptionValue().equals(name));
        if(isConflict){
            throw new DuplicateResourceException(ms.getMessage(OPTION_VALUE_CONFLICT));
        }
    }
}
