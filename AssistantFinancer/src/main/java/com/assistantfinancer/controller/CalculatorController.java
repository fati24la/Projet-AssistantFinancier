package com.assistantfinancer.controller;

import com.assistantfinancer.dto.CalculatorRequest;
import com.assistantfinancer.dto.CalculatorResponse;
import com.assistantfinancer.service.FinancialCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calculators")
public class CalculatorController {

    @Autowired
    private FinancialCalculatorService calculatorService;

    @PostMapping
    public ResponseEntity<CalculatorResponse> calculate(@RequestBody CalculatorRequest request) {
        CalculatorResponse response = calculatorService.calculate(request);
        return ResponseEntity.ok(response);
    }
}

