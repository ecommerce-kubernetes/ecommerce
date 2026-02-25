package com.example.userservice.api.user.saga.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "user.topics")
public class UserTopicProperties {
    private String userSagaCommand;
    private String userSagaReply;
}
