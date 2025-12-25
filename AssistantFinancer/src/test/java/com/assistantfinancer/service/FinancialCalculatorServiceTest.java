package com.assistantfinancer.service;

import com.assistantfinancer.dto.CalculatorRequest;
import com.assistantfinancer.dto.CalculatorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FinancialCalculatorServiceTest {

    @InjectMocks
    private FinancialCalculatorService calculatorService;

    @BeforeEach
    void setUp() {
        calculatorService = new FinancialCalculatorService();
    }

    @Test
    void calculate_WithCreditType_ShouldCalculateCorrectly() {
        // Given
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("CREDIT");
        request.setPrincipal(new BigDecimal("100000.00"));
        request.setInterestRate(new BigDecimal("5.0"));
        request.setDurationMonths(240); // 20 years

        // When
        CalculatorResponse response = calculatorService.calculate(request);

        // Then
        assertNotNull(response);
        assertEquals("CREDIT", response.getCalculatorType());
        assertNotNull(response.getResults());
        assertTrue(response.getResults().containsKey("monthlyPayment"));
        assertTrue(response.getResults().containsKey("totalAmount"));
        assertTrue(response.getResults().containsKey("totalInterest"));
    }

    @Test
    void calculate_WithSavingsType_ShouldCalculateCorrectly() {
        // Given
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("SAVINGS");
        request.setTargetAmount(new BigDecimal("10000.00"));
        request.setMonthlyContribution(new BigDecimal("500.00"));
        request.setCurrentSavings(new BigDecimal("1000.00"));
        request.setInterestRate(new BigDecimal("3.0"));

        // When
        CalculatorResponse response = calculatorService.calculate(request);

        // Then
        assertNotNull(response);
        assertEquals("SAVINGS", response.getCalculatorType());
        assertNotNull(response.getResults());
        assertTrue(response.getResults().containsKey("monthsNeeded"));
        assertTrue(response.getResults().containsKey("totalContribution"));
    }

    @Test
    void calculate_WithInvestmentType_ShouldCalculateCorrectly() {
        // Given
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("INVESTMENT");
        request.setPrincipal(new BigDecimal("50000.00"));
        request.setExpectedReturn(new BigDecimal("8.0"));
        request.setDurationMonths(120); // 10 years

        // When
        CalculatorResponse response = calculatorService.calculate(request);

        // Then
        assertNotNull(response);
        assertEquals("INVESTMENT", response.getCalculatorType());
        assertNotNull(response.getResults());
        assertTrue(response.getResults().containsKey("futureValue"));
        assertTrue(response.getResults().containsKey("profit"));
    }

    @Test
    void calculate_WithBorrowingCapacityType_ShouldCalculateCorrectly() {
        // Given
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("BORROWING_CAPACITY");
        request.setMonthlyIncome(new BigDecimal("5000.00"));
        request.setMonthlyExpenses(new BigDecimal("2000.00"));
        request.setOtherDebts(new BigDecimal("500.00"));

        // When
        CalculatorResponse response = calculatorService.calculate(request);

        // Then
        assertNotNull(response);
        assertEquals("BORROWING_CAPACITY", response.getCalculatorType());
        assertNotNull(response.getResults());
        assertTrue(response.getResults().containsKey("borrowingCapacity"));
        assertTrue(response.getResults().containsKey("maxMonthlyPayment"));
    }

    @Test
    void calculate_WithUnsupportedType_ShouldThrowException() {
        // Given
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("UNSUPPORTED");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            calculatorService.calculate(request);
        });

        assertEquals("Calculator type not supported", exception.getMessage());
    }

    @Test
    void calculateCredit_ShouldCalculateMonthlyPaymentCorrectly() {
        // Given
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("CREDIT");
        request.setPrincipal(new BigDecimal("100000.00"));
        request.setInterestRate(new BigDecimal("5.0"));
        request.setDurationMonths(240);

        // When
        CalculatorResponse response = calculatorService.calculate(request);
        Map<String, Object> results = response.getResults();

        // Then
        BigDecimal monthlyPayment = (BigDecimal) results.get("monthlyPayment");
        assertNotNull(monthlyPayment);
        assertTrue(monthlyPayment.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateCredit_ShouldCalculateTotalInterestCorrectly() {
        // Given
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("CREDIT");
        request.setPrincipal(new BigDecimal("100000.00"));
        request.setInterestRate(new BigDecimal("5.0"));
        request.setDurationMonths(240);

        // When
        CalculatorResponse response = calculatorService.calculate(request);
        Map<String, Object> results = response.getResults();

        // Then
        BigDecimal totalInterest = (BigDecimal) results.get("totalInterest");
        assertNotNull(totalInterest);
        assertTrue(totalInterest.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateCredit_WithMissingParameters_ShouldThrowException() {
        // Given
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("CREDIT");
        request.setPrincipal(new BigDecimal("100000.00"));
        // Missing interestRate and durationMonths

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            calculatorService.calculate(request);
        });

        assertEquals("Missing required parameters for credit calculation", exception.getMessage());
    }

    @Test
    void calculateSavings_WithoutInterest_ShouldCalculateMonthsNeeded() {
        // Given
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("SAVINGS");
        request.setTargetAmount(new BigDecimal("10000.00"));
        request.setMonthlyContribution(new BigDecimal("500.00"));
        request.setCurrentSavings(new BigDecimal("1000.00"));
        request.setInterestRate(BigDecimal.ZERO);

        // When
        CalculatorResponse response = calculatorService.calculate(request);
        Map<String, Object> results = response.getResults();

        // Then
        Integer monthsNeeded = (Integer) results.get("monthsNeeded");
        assertNotNull(monthsNeeded);
        assertTrue(monthsNeeded > 0);
    }

    @Test
    void calculateSavings_WithInterest_ShouldCalculateWithInterest() {
        // Given
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("SAVINGS");
        request.setTargetAmount(new BigDecimal("10000.00"));
        request.setMonthlyContribution(new BigDecimal("500.00"));
        request.setCurrentSavings(new BigDecimal("1000.00"));
        request.setInterestRate(new BigDecimal("3.0"));

        // When
        CalculatorResponse response = calculatorService.calculate(request);
        Map<String, Object> results = response.getResults();

        // Then
        Integer monthsNeeded = (Integer) results.get("monthsNeeded");
        assertNotNull(monthsNeeded);
        assertTrue(monthsNeeded > 0);
    }

    @Test
    void calculateSavings_WithMissingParameters_ShouldThrowException() {
        // Given
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("SAVINGS");
        request.setTargetAmount(new BigDecimal("10000.00"));
        // Missing monthlyContribution

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            calculatorService.calculate(request);
        });

        assertEquals("Missing required parameters for savings calculation", exception.getMessage());
    }

    @Test
    void calculateInvestment_ShouldCalculateFutureValueCorrectly() {
        // Given
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("INVESTMENT");
        request.setPrincipal(new BigDecimal("50000.00"));
        request.setExpectedReturn(new BigDecimal("8.0"));
        request.setDurationMonths(120);

        // When
        CalculatorResponse response = calculatorService.calculate(request);
        Map<String, Object> results = response.getResults();

        // Then
        BigDecimal futureValue = (BigDecimal) results.get("futureValue");
        assertNotNull(futureValue);
        assertTrue(futureValue.compareTo(new BigDecimal("50000.00")) > 0);
    }

    @Test
    void calculateInvestment_ShouldCalculateProfitCorrectly() {
        // Given
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("INVESTMENT");
        request.setPrincipal(new BigDecimal("50000.00"));
        request.setExpectedReturn(new BigDecimal("8.0"));
        request.setDurationMonths(120);

        // When
        CalculatorResponse response = calculatorService.calculate(request);
        Map<String, Object> results = response.getResults();

        // Then
        BigDecimal profit = (BigDecimal) results.get("profit");
        assertNotNull(profit);
        assertTrue(profit.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateBorrowingCapacity_ShouldCalculateCapacity() {
        // Given
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("BORROWING_CAPACITY");
        request.setMonthlyIncome(new BigDecimal("5000.00"));
        request.setMonthlyExpenses(new BigDecimal("2000.00"));
        request.setOtherDebts(new BigDecimal("500.00"));

        // When
        CalculatorResponse response = calculatorService.calculate(request);
        Map<String, Object> results = response.getResults();

        // Then
        BigDecimal borrowingCapacity = (BigDecimal) results.get("borrowingCapacity");
        assertNotNull(borrowingCapacity);
        assertTrue(borrowingCapacity.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateBorrowingCapacity_ShouldConsiderExistingDebts() {
        // Given
        CalculatorRequest request = new CalculatorRequest();
        request.setCalculatorType("BORROWING_CAPACITY");
        request.setMonthlyIncome(new BigDecimal("5000.00"));
        request.setMonthlyExpenses(new BigDecimal("2000.00"));
        request.setOtherDebts(new BigDecimal("1000.00"));

        // When
        CalculatorResponse response = calculatorService.calculate(request);
        Map<String, Object> results = response.getResults();

        // Then
        BigDecimal netIncome = (BigDecimal) results.get("netIncome");
        assertNotNull(netIncome);
        // netIncome = 5000 - 2000 - 1000 = 2000
        assertEquals(new BigDecimal("2000.00"), netIncome);
    }
}

