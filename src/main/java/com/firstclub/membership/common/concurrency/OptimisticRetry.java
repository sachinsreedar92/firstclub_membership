package com.firstclub.membership.common.concurrency;

import com.firstclub.membership.common.exception.ConflictException;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

/**
 * Small helper that retries an action a bounded number of times when an optimistic-lock
 * conflict occurs. Used for concurrent subscription mutations (upgrade/downgrade/auto-tier),
 * where two threads may race on the same versioned row; the loser simply retries on fresh state.
 */
@Component
public class OptimisticRetry {

    private static final Logger log = LoggerFactory.getLogger(OptimisticRetry.class);
    private static final int MAX_ATTEMPTS = 4;

    public <T> T execute(String operation, Supplier<T> action) {
        OptimisticLockingFailureException last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return action.get();
            } catch (OptimisticLockingFailureException ex) {
                last = ex;
                log.warn("Optimistic conflict on '{}' (attempt {}/{}), retrying",
                        operation, attempt, MAX_ATTEMPTS);
            }
        }
        throw new ConflictException(
                "Could not complete '" + operation + "' due to concurrent updates: "
                        + (last == null ? "" : last.getMessage()));
    }
}
