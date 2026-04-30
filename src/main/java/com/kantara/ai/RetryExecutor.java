package com.kantara.ai;

import com.kantara.exception.AiException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class RetryExecutor {
    private static final Logger LOGGER = Logger.getLogger(RetryExecutor.class.getName());

    public <T> T execute(Supplier<T> action, RetryPolicy policy) {
        int attempt = 1;
        long currentDelay = policy.initialDelayMs();

        while (true) {
            try {
                return action.get();
            } catch (AiException e) {
                if (!e.isRetryable() || attempt >= policy.maxAttempts()) {
                    throw e;
                }
                
                LOGGER.warning(String.format("[Kantara] Retryable AI error: %s. Retrying %d/%d in %d ms...",
                        e.getMessage(), attempt, policy.maxAttempts() - 1, currentDelay));
                
                try {
                    Thread.sleep(currentDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AiException("Retry sleep was interrupted.", ie);
                }

                attempt++;
                currentDelay = computeNextDelay(currentDelay, policy);
            }
        }
    }

    private long computeNextDelay(long currentDelay, RetryPolicy policy) {
        long nextDelay = (long) (currentDelay * policy.multiplier());
        // Add ±20% jitter
        double jitter = 0.8 + (0.4 * ThreadLocalRandom.current().nextDouble());
        long delayWithJitter = (long) (nextDelay * jitter);
        return Math.min(delayWithJitter, policy.maxDelayMs());
    }
}
