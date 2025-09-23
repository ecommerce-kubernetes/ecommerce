package com.example.product_service.controller.util.validator;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.entity.DomainType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.product_service.common.MessagePath.*;

@Component
@RequiredArgsConstructor
public class PageableValidatorFactory {
    private final List<PageableValidator> pageableValidatorList;
    private final MessageSourceUtil ms;

    public PageableValidator getValidator(DomainType domainType){
        return pageableValidatorList
                .stream().filter(pv -> pv.support(domainType))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(ms.getMessage(NOT_SUPPORTED_DOMAIN)));
    }
}
