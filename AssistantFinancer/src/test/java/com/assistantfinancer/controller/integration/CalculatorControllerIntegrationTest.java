package com.assistantfinancer.controller.integration;

import com.assistantfinancer.dto.CalculatorRequest;
import com.assistantfinancer.dto.CalculatorResponse;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CalculatorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser = testHelper.createTestUser("testuser", "test@example.com", "password123");
        authToken = testHelper.generateTokenForUser(testUser);
    }

    @Test
    void testCalculateLoan_ShouldReturnMonthlyPayment() throws Exception {
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("CREDIT");
        request.setPrincipal(new BigDecimal("100000.00"));
        request.setInterestRate(new BigDecimal("5.0"));
        request.setDurationMonths(240); // 20 ans

        String response = mockMvc.perform(post("/api/calculators")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculatorType").value("CREDIT"))
                .andExpect(jsonPath("$.results.monthlyPayment").exists())
                .andExpect(jsonPath("$.results.totalAmount").exists())
                .andExpect(jsonPath("$.results.totalInterest").exists())
                .andExpect(jsonPath("$.explanation").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CalculatorResponse calculatorResponse = objectMapper.readValue(response, CalculatorResponse.class);
        assertNotNull(calculatorResponse.getResults());
        assertTrue(calculatorResponse.getResults().containsKey("monthlyPayment"));
        
        BigDecimal monthlyPayment = new BigDecimal(calculatorResponse.getResults().get("monthlyPayment").toString());
        assertTrue(monthlyPayment.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalculateSavings_ShouldReturnMonthsNeeded() throws Exception {
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("SAVINGS");
        request.setTargetAmount(new BigDecimal("10000.00"));
        request.setMonthlyContribution(new BigDecimal("500.00"));
        request.setCurrentSavings(new BigDecimal("1000.00"));
        request.setInterestRate(new BigDecimal("3.0"));

        String response = mockMvc.perform(post("/api/calculators")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculatorType").value("SAVINGS"))
                .andExpect(jsonPath("$.results.monthsNeeded").exists())
                .andExpect(jsonPath("$.results.totalContribution").exists())
                .andExpect(jsonPath("$.explanation").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CalculatorResponse calculatorResponse = objectMapper.readValue(response, CalculatorResponse.class);
        assertNotNull(calculatorResponse.getResults());
        assertTrue(calculatorResponse.getResults().containsKey("monthsNeeded"));
        
        Integer monthsNeeded = Integer.valueOf(calculatorResponse.getResults().get("monthsNeeded").toString());
        assertTrue(monthsNeeded > 0);
    }

    @Test
    void testCalculateInvestment_ShouldReturnFutureValue() throws Exception {
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("INVESTMENT");
        request.setPrincipal(new BigDecimal("5000.00"));
        request.setExpectedReturn(new BigDecimal("7.0"));
        request.setDurationMonths(120); // 10 ans

        String response = mockMvc.perform(post("/api/calculators")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculatorType").value("INVESTMENT"))
                .andExpect(jsonPath("$.results.futureValue").exists())
                .andExpect(jsonPath("$.results.profit").exists())
                .andExpect(jsonPath("$.explanation").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CalculatorResponse calculatorResponse = objectMapper.readValue(response, CalculatorResponse.class);
        assertNotNull(calculatorResponse.getResults());
        assertTrue(calculatorResponse.getResults().containsKey("futureValue"));
        
        BigDecimal futureValue = new BigDecimal(calculatorResponse.getResults().get("futureValue").toString());
        assertTrue(futureValue.compareTo(new BigDecimal("5000.00")) > 0);
    }

    @Test
    void testCalculateBorrowingCapacity_ShouldReturnCapacity() throws Exception {
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("BORROWING_CAPACITY");
        request.setMonthlyIncome(new BigDecimal("5000.00"));
        request.setMonthlyExpenses(new BigDecimal("2000.00"));
        request.setOtherDebts(new BigDecimal("500.00"));

        String response = mockMvc.perform(post("/api/calculators")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculatorType").value("BORROWING_CAPACITY"))
                .andExpect(jsonPath("$.results.borrowingCapacity").exists())
                .andExpect(jsonPath("$.results.maxMonthlyPayment").exists())
                .andExpect(jsonPath("$.results.netIncome").exists())
                .andExpect(jsonPath("$.explanation").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CalculatorResponse calculatorResponse = objectMapper.readValue(response, CalculatorResponse.class);
        assertNotNull(calculatorResponse.getResults());
        assertTrue(calculatorResponse.getResults().containsKey("borrowingCapacity"));
        
        BigDecimal borrowingCapacity = new BigDecimal(calculatorResponse.getResults().get("borrowingCapacity").toString());
        assertTrue(borrowingCapacity.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalculateLoan_WithDifferentRates_ShouldReturnDifferentResults() throws Exception {
        // Test avec taux bas
        CalculatorRequest request1 = new CalculatorRequest();
        request1.setCalculatorType("CREDIT");
        request1.setPrincipal(new BigDecimal("100000.00"));
        request1.setInterestRate(new BigDecimal("3.0"));
        request1.setDurationMonths(240);

        String response1 = mockMvc.perform(post("/api/calculators")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CalculatorResponse response1Obj = objectMapper.readValue(response1, CalculatorResponse.class);
        BigDecimal monthlyPayment1 = new BigDecimal(response1Obj.getResults().get("monthlyPayment").toString());

        // Test avec taux élevé
        CalculatorRequest request2 = new CalculatorRequest();
        request2.setCalculatorType("CREDIT");
        request2.setPrincipal(new BigDecimal("100000.00"));
        request2.setInterestRate(new BigDecimal("8.0"));
        request2.setDurationMonths(240);

        String response2 = mockMvc.perform(post("/api/calculators")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CalculatorResponse response2Obj = objectMapper.readValue(response2, CalculatorResponse.class);
        BigDecimal monthlyPayment2 = new BigDecimal(response2Obj.getResults().get("monthlyPayment").toString());

        // Le paiement mensuel avec un taux plus élevé devrait être plus élevé
        assertTrue(monthlyPayment2.compareTo(monthlyPayment1) > 0);
    }

    @Test
    void testCalculate_WithInvalidType_ShouldReturnError() throws Exception {
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("INVALID_TYPE");
        request.setPrincipal(new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/calculators")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // RuntimeException non gérée devient 500
                    assertTrue(status >= 400 && status < 600, 
                        () -> "Expected error status but got: " + status);
                });
    }

    @Test
    void testCalculate_WithMissingParameters_ShouldReturnError() throws Exception {
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("CREDIT");
        // Missing principal, interestRate, durationMonths

        mockMvc.perform(post("/api/calculators")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // RuntimeException non gérée devient 500
                    assertTrue(status >= 400 && status < 600, 
                        () -> "Expected error status but got: " + status);
                });
    }
}

