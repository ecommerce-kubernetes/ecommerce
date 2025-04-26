package com.example.config_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.cloud.config.server.encrypt.enabled=false",
		"encrypt.key-store.location=classpath:dummy.jks"
})
class ConfigServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
