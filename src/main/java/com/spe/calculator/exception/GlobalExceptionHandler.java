package com.spe.calculator.exception;

import com.spe.calculator.model.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

/**
 * Turns bad input into HTTP 400 with a consistent {@link ApiError} JSON body
 * instead of Spring's default error page or a 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DivisionByZeroException.class, InvalidOperandException.class})
    public ResponseEntity<ApiError> handleInvalidCalculation(RuntimeException ex, HttpServletRequest request) {
        return badRequest(ex.getMessage(), request);
    }

    /** {@code /add?a=2} */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException ex,
                                                           HttpServletRequest request) {
        return badRequest("Missing required parameter '" + ex.getParameterName() + "'", request);
    }

    /** {@code /add?a=two&b=3} or {@code /add?a=2&b=} */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest request) {
        return badRequest("Parameter '" + ex.getName() + "' must be a number, got '" + ex.getValue() + "'", request);
    }

    private static ResponseEntity<ApiError> badRequest(String message, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
