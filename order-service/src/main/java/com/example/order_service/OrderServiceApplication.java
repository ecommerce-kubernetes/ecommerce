package com.example.order_service;

import com.example.order_service.infrastructure.config.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableDiscoveryClient
@EnableFeignClients(defaultConfiguration = DefaultFeignConfig.class)
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}
