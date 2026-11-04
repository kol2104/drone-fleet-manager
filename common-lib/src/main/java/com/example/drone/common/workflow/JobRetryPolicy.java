package com.example.drone.common.workflow;

import java.time.Duration;

public final class JobRetryPolicy {

    private static final Duration RETRY_BACKOFF = Duration.ofSeconds(5);

    private JobRetryPolicy() {
    }

    public static int retriesAfterFailure(int currentRetries) {
        if (currentRetries < 0) {
            throw new IllegalArgumentException("Current retries must not be negative");
        }
        return Math.max(0, currentRetries - 1);
    }

    public static Duration retryBackoff() {
        return RETRY_BACKOFF;
    }

    public static String errorMessage(Throwable failure) {
        String message = failure.getMessage();
        return message == null || message.isBlank() ? failure.getClass().getSimpleName() : message;
    }
}
