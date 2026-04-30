package com.kantara.ai;

public record RetryPolicy(
    int maxAttempts,
    long initialDelayMs,
    long maxDelayMs,
    double multiplier
) {
    public static final RetryPolicy DEFAULT = new RetryPolicy(3, 1000, 10000, 2.0);
}
