package com.example.config_service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;


@TestConfiguration
public class TestEncryptConfiguration {

    @Bean
    @Primary
    public TextEncryptor textEncryptor() {
        // 테스트용으로 Dummy Encryptor를 씁니다 (실제 암호화 X)
        return Encryptors.noOpText();
    }
}
