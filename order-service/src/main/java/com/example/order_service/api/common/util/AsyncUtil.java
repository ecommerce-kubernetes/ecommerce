package com.example.order_service.api.common.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AsyncUtil {
    private AsyncUtil() {}

    public static <T> T join(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();

            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            throw e;
        }
    }
}
