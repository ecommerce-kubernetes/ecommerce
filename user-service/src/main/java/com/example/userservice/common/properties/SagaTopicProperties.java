package com.example.userservice.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "user.topics.saga")
public record SagaTopicProperties(
        TopicConfig order
) {

    public record TopicConfig(
            String command,
            String reply
    ) {
    }
}
