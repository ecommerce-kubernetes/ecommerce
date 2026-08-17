package com.example.order_service.saga.exception;

public class MessageSerializationException extends SagaSystemException {
    public MessageSerializationException(String message) {
        super(message);
    }
}
