package com.example.config_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"encrypt.key-store.secret=dummy-secret"
})
class ConfigServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
