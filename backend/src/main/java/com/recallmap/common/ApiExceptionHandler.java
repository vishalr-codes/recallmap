package com.recallmap.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleInvalidArguments(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<ApiViolation> violations = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiViolation(
                        error.getField(),
                        error.getDefaultMessage() == null ? "Invalid value." : error.getDefaultMessage()))
                .sorted(Comparator.comparing(ApiViolation::field).thenComparing(ApiViolation::message))
                .toList();
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "Request validation failed.", request, violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        List<ApiViolation> violations = exception.getConstraintViolations().stream()
                .map(violation -> new ApiViolation(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .sorted(Comparator.comparing(ApiViolation::field).thenComparing(ApiViolation::message))
                .toList();
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "Request validation failed.", request, violations);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleUnreadableBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "Request body is missing or malformed.", request, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ProblemDetail handleMissingParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
                "A required request parameter is missing.", request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail handleNotFound(
            NoResourceFoundException exception,
            HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "NOT_FOUND",
                "The requested resource was not found.", request, null);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(
            Exception exception,
            HttpServletRequest request) {
        logger.error("Unhandled request failure; exceptionType={}", exception.getClass().getName());
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred.", request, null);
    }

    private ProblemDetail problem(
            HttpStatus status,
            String errorCode,
            String detail,
            HttpServletRequest request,
            List<ApiViolation> violations) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setInstance(java.net.URI.create(request.getRequestURI()));
        problem.setProperty("errorCode", errorCode);
        problem.setProperty("timestamp", Instant.now());
        if (violations != null && !violations.isEmpty()) {
            problem.setProperty("violations", violations);
        }
        return problem;
    }

    private record ApiViolation(String field, String message) {
    }
}
