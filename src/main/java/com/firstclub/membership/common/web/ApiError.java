package com.firstclub.membership.common.web;

import java.time.Instant;
import java.util.List;

/** Consistent error payload returned by the global exception handler. */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String traceId,
        List<String> details) {
}
