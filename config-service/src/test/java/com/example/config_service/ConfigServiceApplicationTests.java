package com.example.config_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@ImportAutoConfiguration(exclude = {
		org.springframework.cloud.bootstrap.encrypt.EncryptionBootstrapConfiguration.class,
		org.springframework.cloud.config.server.config.RsaEncryptionAutoConfiguration.class
})
class ConfigServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
