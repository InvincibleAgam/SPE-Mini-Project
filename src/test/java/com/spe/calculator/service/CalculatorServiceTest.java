package com.spe.calculator.service;

import com.spe.calculator.exception.DivisionByZeroException;
import com.spe.calculator.exception.InvalidOperandException;
import com.spe.calculator.model.CalculationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Plain unit tests: no Spring context, just the service.
 */
class CalculatorServiceTest {

    private final CalculatorService calculator = new CalculatorService();

    /** Compares by numeric value so 5 and 5.0 are treated as equal. */
    private static void assertResult(String expected, CalculationResult actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual.result()),
                () -> "expected " + expected + " but was " + actual.result());
    }

    @Nested
    @DisplayName("add")
    class Add {

        @ParameterizedTest(name = "{0} + {1} = {2}")
        @CsvSource({
                "2, 3, 5",
                "-4, 10, 6",
                "-2.5, -2.5, -5",
                "0, 0, 0",
                "0.1, 0.2, 0.3"   // would be 0.30000000000000004 with plain doubles
        })
        void addsTwoNumbers(double a, double b, String expected) {
            assertResult(expected, calculator.add(a, b));
        }

        @Test
        void echoesOperationAndOperands() {
            CalculationResult result = calculator.add(2, 3);

            assertEquals("add", result.operation());
            assertEquals(new BigDecimal("2"), result.a());
            assertEquals(new BigDecimal("3"), result.b());
            assertEquals(new BigDecimal("5"), result.result());
        }
    }

    @Nested
    @DisplayName("subtract")
    class Subtract {

        @ParameterizedTest(name = "{0} - {1} = {2}")
        @CsvSource({
                "10, 4, 6",
                "4, 10, -6",
                "-3, -3, 0",
                "0.3, 0.1, 0.2"
        })
        void subtractsTwoNumbers(double a, double b, String expected) {
            CalculationResult result = calculator.subtract(a, b);

            assertEquals("subtract", result.operation());
            assertResult(expected, result);
        }
    }

    @Nested
    @DisplayName("multiply")
    class Multiply {

        @ParameterizedTest(name = "{0} * {1} = {2}")
        @CsvSource({
                "6, 7, 42",
                "-3, 4, -12",
                "-3, -4, 12",
                "123456, 0, 0",
                "1.5, 1.5, 2.25"
        })
        void multipliesTwoNumbers(double a, double b, String expected) {
            CalculationResult result = calculator.multiply(a, b);

            assertEquals("multiply", result.operation());
            assertResult(expected, result);
        }
    }

    @Nested
    @DisplayName("divide")
    class Divide {

        @ParameterizedTest(name = "{0} / {1} = {2}")
        @CsvSource({
                "10, 2, 5",
                "7, 2, 3.5",
                "-9, 3, -3",
                "0, 5, 0",
                "1, 3, 0.3333333333333333",
                "2, 3, 0.6666666666666667"
        })
        void dividesTwoNumbers(double a, double b, String expected) {
            CalculationResult result = calculator.divide(a, b);

            assertEquals("divide", result.operation());
            assertResult(expected, result);
        }

        @ParameterizedTest(name = "{0} / 0 is rejected")
        @ValueSource(doubles = {1, -1, 0})
        void rejectsDivisionByZero(double a) {
            DivisionByZeroException ex = assertThrows(DivisionByZeroException.class, () -> calculator.divide(a, 0));
            assertEquals("Division by zero is not allowed", ex.getMessage());
        }

        @Test
        void rejectsDivisionByNegativeZero() {
            assertThrows(DivisionByZeroException.class, () -> calculator.divide(1, -0.0));
        }
    }

    @Nested
    @DisplayName("input validation")
    class Validation {

        @ParameterizedTest(name = "a = {0} is rejected")
        @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
        void rejectsNonFiniteFirstOperand(double a) {
            InvalidOperandException ex = assertThrows(InvalidOperandException.class, () -> calculator.add(a, 1));
            assertEquals("Parameter 'a' must be a finite number", ex.getMessage());
        }

        @Test
        void rejectsNonFiniteSecondOperand() {
            InvalidOperandException ex = assertThrows(InvalidOperandException.class,
                    () -> calculator.multiply(1, Double.POSITIVE_INFINITY));
            assertEquals("Parameter 'b' must be a finite number", ex.getMessage());
        }

        @Test
        void handlesResultsBeyondDoubleRange() {
            // 1e308 * 10 overflows a double to Infinity; BigDecimal has no such limit
            assertResult("1E+309", calculator.multiply(1e308, 10));
        }
    }
}
