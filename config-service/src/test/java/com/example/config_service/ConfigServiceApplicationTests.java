package com.example.config_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@ImportAutoConfiguration(exclude = {
		org.springframework.cloud.bootstrap.encrypt.EncryptionBootstrapConfiguration.class
})
class ConfigServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
