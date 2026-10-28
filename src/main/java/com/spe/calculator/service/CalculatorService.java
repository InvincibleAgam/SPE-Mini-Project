package com.spe.calculator.service;

import com.spe.calculator.exception.DivisionByZeroException;
import com.spe.calculator.exception.InvalidOperandException;
import com.spe.calculator.model.CalculationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.function.BinaryOperator;

/**
 * The arithmetic itself. Inputs arrive as doubles but the maths is done in BigDecimal,
 * so 0.1 + 0.2 returns 0.3 instead of the binary floating-point 0.30000000000000004.
 */
@Service
public class CalculatorService {

    /** 16 significant digits: the same precision as a double, rounded half-even. */
    private static final MathContext PRECISION = MathContext.DECIMAL64;

    public CalculationResult add(double a, double b) {
        return calculate("add", a, b, (x, y) -> x.add(y, PRECISION));
    }

    public CalculationResult subtract(double a, double b) {
        return calculate("subtract", a, b, (x, y) -> x.subtract(y, PRECISION));
    }

    public CalculationResult multiply(double a, double b) {
        return calculate("multiply", a, b, (x, y) -> x.multiply(y, PRECISION));
    }

    public CalculationResult divide(double a, double b) {
        return calculate("divide", a, b, (x, y) -> {
            if (y.signum() == 0) {
                throw new DivisionByZeroException();
            }
            return x.divide(y, PRECISION);
        });
    }

    private CalculationResult calculate(String operation, double a, double b, BinaryOperator<BigDecimal> operator) {
        BigDecimal x = toDecimal("a", a);
        BigDecimal y = toDecimal("b", b);
        return new CalculationResult(operation, x, y, operator.apply(x, y).stripTrailingZeros());
    }

    private static BigDecimal toDecimal(String name, double value) {
        // Spring happily parses "NaN", "Infinity" and "1e999" into a double, so reject them here
        if (!Double.isFinite(value)) {
            throw new InvalidOperandException("Parameter '" + name + "' must be a finite number");
        }
        return BigDecimal.valueOf(value).stripTrailingZeros();
    }
}
