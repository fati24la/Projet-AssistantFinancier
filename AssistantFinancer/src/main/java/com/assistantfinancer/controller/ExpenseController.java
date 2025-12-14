package com.assistantfinancer.controller;

import com.assistantfinancer.dto.ExpenseDto;
import com.assistantfinancer.service.ExpenseService;
import com.assistantfinancer.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserUtil userUtil;

    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(@RequestBody ExpenseDto expenseDto) {
        Long userId = userUtil.getUserIdFromAuth();
        ExpenseDto created = expenseService.createExpense(userId, expenseDto);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getUserExpenses() {
        Long userId = userUtil.getUserIdFromAuth();
        List<ExpenseDto> expenses = expenseService.getUserExpenses(userId);
        return ResponseEntity.ok(expenses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto> updateExpense(@PathVariable Long id, @RequestBody ExpenseDto expenseDto) {
        Long userId = userUtil.getUserIdFromAuth();
        ExpenseDto updated = expenseService.updateExpense(userId, id, expenseDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        Long userId = userUtil.getUserIdFromAuth();
        expenseService.deleteExpense(userId, id);
        return ResponseEntity.ok().build();
    }
}

