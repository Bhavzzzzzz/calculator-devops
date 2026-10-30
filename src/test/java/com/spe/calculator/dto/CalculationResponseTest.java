package com.spe.calculator.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculationResponseTest {

    @Test
    void wholeNumbersAreNormalizedToLong() {
        assertEquals(5L, CalculationResponse.normalize(5.0));
        assertEquals(-3L, CalculationResponse.normalize(-3.0));
        assertEquals(0L, CalculationResponse.normalize(-0.0));
    }

    @Test
    void fractionalAndHugeNumbersStayDouble() {
        assertEquals(2.5, CalculationResponse.normalize(2.5));
        assertEquals(1e20, CalculationResponse.normalize(1e20));
    }

    @Test
    void factoryNormalizesAllNumericFields() {
        CalculationResponse response = CalculationResponse.of("add", 2.0, 3.0, 5.0);
        assertEquals(new CalculationResponse("add", 2L, 3L, 5L), response);
    }
}
