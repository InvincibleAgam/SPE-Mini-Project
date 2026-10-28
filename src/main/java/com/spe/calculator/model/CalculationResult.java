package com.spe.calculator.model;

import java.math.BigDecimal;

/**
 * Successful response body, e.g. {@code {"operation":"add","a":2,"b":3,"result":5}}.
 */
public record CalculationResult(String operation, BigDecimal a, BigDecimal b, BigDecimal result) {
}
