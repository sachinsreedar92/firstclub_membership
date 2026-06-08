package com.firstclub.membership.common.web;

import com.firstclub.membership.common.exception.BadRequestException;
import com.firstclub.membership.common.exception.ConflictException;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Translates domain and infrastructure exceptions into consistent {@link ApiError} responses,
 * including the request's correlation id.  Resilience4j rejections map to 429 so clients can back off.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex,
                                                   HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex,
                                                     HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimistic(OptimisticLockingFailureException ex,
                                                     HttpServletRequest req) {
        return build(HttpStatus.CONFLICT,
                "The resource was modified concurrently — please retry.", req, List.of());
    }

    /** Bean/method-level @Valid validation failures. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest req) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        List<String> globalErrors = ex.getBindingResult().getGlobalErrors().stream()
                .map(ge -> ge.getDefaultMessage())
                .toList();
        List<String> all = details.isEmpty() && !globalErrors.isEmpty() ? globalErrors
                : details.isEmpty() ? List.of() : details;
        return build(HttpStatus.BAD_REQUEST,
                "Request validation failed — see 'details' for field-level errors.", req, all);
    }

    /** @Validated on service methods / path variables. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex,
                                                     HttpServletRequest req) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(cv -> leafPath(cv) + ": " + cv.getMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST,
                "Request validation failed — see 'details' for field-level errors.", req, details);
    }

    /** Malformed JSON body or unrecognised enum value. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex,
                                                     HttpServletRequest req) {
        String msg = ex.getMostSpecificCause().getMessage();
        return build(HttpStatus.BAD_REQUEST,
                "Malformed request body: " + sanitise(msg), req, List.of());
    }

    /** Missing required request parameter. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex,
                                                       HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "Required query parameter '" + ex.getParameterName() + "' is missing.", req, List.of());
    }

    /** Path variable type mismatch (e.g. non-numeric id). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest req) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        return build(HttpStatus.BAD_REQUEST,
                "Parameter '" + ex.getName() + "' must be of type " + expected + ".", req, List.of());
    }

     @ExceptionHandler({RequestNotPermitted.class, BulkheadFullException.class})
     public ResponseEntity<ApiError> handleThrottled(RuntimeException ex, HttpServletRequest req) {
         log.warn("Request throttled: {}", ex.getMessage());
         return build(HttpStatus.TOO_MANY_REQUESTS,
                 "Too many requests — please retry shortly.", req, List.of());
     }

     /**
      * Handles circuit breaker open state. Circuit opens when downstream service is failing
      * or responding slowly. Clients should back off and retry after some delay.
      */
     @ExceptionHandler(CallNotPermittedException.class)
     public ResponseEntity<ApiError> handleCircuitOpen(CallNotPermittedException ex,
                                                      HttpServletRequest req) {
         log.warn("Circuit breaker open: {}", ex.getMessage());
         return build(HttpStatus.SERVICE_UNAVAILABLE,
                 "Service temporarily unavailable — please retry after a short delay.", req, List.of());
     }

     @ExceptionHandler(Exception.class)
     public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {} {}", req.getMethod(), req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again or contact support if the problem persists.",
                req, List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message,
                                           HttpServletRequest req, List<String> details) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                req.getRequestURI(),
                MDC.get("traceId"),
                details);
        return ResponseEntity.status(status).body(body);
    }

    private static String leafPath(ConstraintViolation<?> cv) {
        String path = cv.getPropertyPath().toString();
        int dot = path.lastIndexOf('.');
        return dot >= 0 ? path.substring(dot + 1) : path;
    }

    private static String sanitise(String msg) {
        if (msg == null) return "unknown error";
        // Trim overly verbose Jackson messages to keep the response concise.
        return msg.length() > 200 ? msg.substring(0, 200) + "…" : msg;
    }
}
