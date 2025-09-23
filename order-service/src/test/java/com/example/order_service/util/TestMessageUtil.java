package com.example.order_service.util;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

public class TestMessageUtil {
    private static final MessageSource messageSource;
    static {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:messages");
        ms.setDefaultEncoding("UTF-8");
        messageSource = ms;
    }
    public static String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, Locale.getDefault());
    }
    public static String getMessage(String code) {
        return messageSource.getMessage(code, null, Locale.getDefault());
    }
}
