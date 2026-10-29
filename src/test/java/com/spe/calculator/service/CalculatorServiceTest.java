package com.spe.calculator.service;

import com.spe.calculator.exception.InvalidOperandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorServiceTest {

    private final CalculatorService service = new CalculatorService();

    @Test
    void addReturnsSum() {
        assertEquals(5, service.add(2, 3));
        assertEquals(-1.5, service.add(-3, 1.5));
    }

    @Test
    void subtractReturnsDifference() {
        assertEquals(-1, service.subtract(2, 3));
        assertEquals(10, service.subtract(7, -3));
    }

    @Test
    void multiplyReturnsProduct() {
        assertEquals(6, service.multiply(2, 3));
        assertEquals(0, service.multiply(0, 42));
    }

    @Test
    void divideReturnsQuotient() {
        assertEquals(2.5, service.divide(5, 2));
        assertEquals(-4, service.divide(8, -2));
    }

    @Test
    void divideByZeroThrows() {
        InvalidOperandException ex = assertThrows(InvalidOperandException.class, () -> service.divide(5, 0));
        assertEquals("Division by zero is not allowed", ex.getMessage());
    }

    @Test
    void nonFiniteOperandsAreRejected() {
        assertThrows(InvalidOperandException.class, () -> service.add(Double.NaN, 1));
        assertThrows(InvalidOperandException.class, () -> service.multiply(1, Double.POSITIVE_INFINITY));
    }
}
