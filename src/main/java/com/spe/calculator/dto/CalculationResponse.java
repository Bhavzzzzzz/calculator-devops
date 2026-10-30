package com.spe.calculator.dto;

public record CalculationResponse(String operation, Number a, Number b, Number result) {

    public static CalculationResponse of(String operation, double a, double b, double result) {
        return new CalculationResponse(operation, normalize(a), normalize(b), normalize(result));
    }

    // Whole numbers are returned as integers so the JSON reads {"result": 5} rather than 5.0
    static Number normalize(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1e15) {
            return (long) value;
        }
        return value;
    }
}
