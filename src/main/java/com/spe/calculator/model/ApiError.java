package com.spe.calculator.model;

import java.time.Instant;

/**
 * Structured error body returned for every 4xx response, e.g.
 * {@code {"timestamp":"...","status":400,"error":"Bad Request","message":"Division by zero is not allowed","path":"/api/v1/divide"}}.
 */
public record ApiError(Instant timestamp, int status, String error, String message, String path) {
}
