package com.example.order_service.common;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageSourceUtil {
    private final MessageSource messageSource;
    public String getMessage(String code){
        return messageSource.getMessage(code, null, Locale.getDefault());
    }
}
