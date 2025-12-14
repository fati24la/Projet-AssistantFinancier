package com.assistantfinancer.controller;

import com.assistantfinancer.dto.BudgetDto;
import com.assistantfinancer.service.BudgetService;
import com.assistantfinancer.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private UserUtil userUtil;

    @PostMapping
    public ResponseEntity<BudgetDto> createBudget(@RequestBody BudgetDto budgetDto) {
        Long userId = userUtil.getUserIdFromAuth();
        BudgetDto created = budgetService.createBudget(userId, budgetDto);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<BudgetDto>> getUserBudgets() {
        Long userId = userUtil.getUserIdFromAuth();
        List<BudgetDto> budgets = budgetService.getUserBudgets(userId);
        return ResponseEntity.ok(budgets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDto> updateBudget(@PathVariable Long id, @RequestBody BudgetDto budgetDto) {
        Long userId = userUtil.getUserIdFromAuth();
        BudgetDto updated = budgetService.updateBudget(userId, id, budgetDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        Long userId = userUtil.getUserIdFromAuth();
        budgetService.deleteBudget(userId, id);
        return ResponseEntity.ok().build();
    }
}

