package com.spe.calculator.service;

import com.spe.calculator.exception.InvalidOperandException;
import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

    public double add(double a, double b) {
        validate(a, b);
        return a + b;
    }

    public double subtract(double a, double b) {
        validate(a, b);
        return a - b;
    }

    public double multiply(double a, double b) {
        validate(a, b);
        return a * b;
    }

    public double divide(double a, double b) {
        validate(a, b);
        if (b == 0) {
            throw new InvalidOperandException("Division by zero is not allowed");
        }
        return a / b;
    }

    // "NaN" and "Infinity" parse as valid doubles, so reject them explicitly
    private void validate(double a, double b) {
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            throw new InvalidOperandException("Operands must be finite numbers");
        }
    }
}
