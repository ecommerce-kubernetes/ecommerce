package com.example.config_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = {
		"spring.cloud.config.server.encrypt.enabled=false"
})
@Import(TestEncryptConfiguration.class)
class ConfigServiceApplicationTests {


	@Test
	void contextLoads() {
	}

}
