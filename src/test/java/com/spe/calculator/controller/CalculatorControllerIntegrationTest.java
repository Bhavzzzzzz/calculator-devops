package com.spe.calculator.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CalculatorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addEndpointReturnsResult() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "2").param("b", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("add"))
                .andExpect(jsonPath("$.a").value(2.0))
                .andExpect(jsonPath("$.b").value(3.0))
                .andExpect(jsonPath("$.result").value(5.0));
    }

    @Test
    void subtractEndpointReturnsResult() throws Exception {
        mockMvc.perform(get("/api/v1/subtract").param("a", "10").param("b", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(6.0));
    }

    @Test
    void multiplyEndpointReturnsResult() throws Exception {
        mockMvc.perform(get("/api/v1/multiply").param("a", "2.5").param("b", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(10.0));
    }

    @Test
    void divideEndpointReturnsResult() throws Exception {
        mockMvc.perform(get("/api/v1/divide").param("a", "9").param("b", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(4.5));
    }

    @Test
    void divideByZeroReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/divide").param("a", "1").param("b", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Division by zero is not allowed"))
                .andExpect(jsonPath("$.path").value("/api/v1/divide"));
    }

    @Test
    void missingParameterReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required parameter 'b'"));
    }

    @Test
    void nonNumericParameterReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/multiply").param("a", "abc").param("b", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'a' must be a number, got 'abc'"));
    }

    @Test
    void healthEndpointIsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
