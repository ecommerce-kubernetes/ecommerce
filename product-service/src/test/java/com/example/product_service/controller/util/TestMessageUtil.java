package com.example.product_service.controller.util;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

public final class TestMessageUtil {
    private static final MessageSource messageSource;
    static {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:ValidationMessages");
        ms.setDefaultEncoding("UTF-8");
        messageSource = ms;
    }

    public static String getMessage(String code) {
        return messageSource.getMessage(code, null, Locale.getDefault());
    }
}
