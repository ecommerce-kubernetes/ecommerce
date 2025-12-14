package com.example.order_service.api.common.util.validator;

import com.example.order_service.api.common.util.DomainType;
import com.example.order_service.api.common.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.order_service.api.common.util.MessagePath.NOT_SUPPORTED_DOMAIN;

@Component
@RequiredArgsConstructor
public class PageableValidatorFactory {
    private final List<PageableValidator> pageableValidatorList;
    private MessageSourceUtil ms;

    public PageableValidator getValidator(DomainType domainType){
        return pageableValidatorList.stream().filter(pv -> pv.support(domainType))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(ms.getMessage(NOT_SUPPORTED_DOMAIN)));
    }
}
