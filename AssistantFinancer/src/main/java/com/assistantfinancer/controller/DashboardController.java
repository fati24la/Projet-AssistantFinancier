package com.assistantfinancer.controller;

import com.assistantfinancer.dto.DashboardDto;
import com.assistantfinancer.service.DashboardService;
import com.assistantfinancer.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserUtil userUtil;

    @GetMapping
    public ResponseEntity<DashboardDto> getDashboard() {
        try {
            Long userId = userUtil.getUserIdFromAuth();
            DashboardDto dashboard = dashboardService.getDashboard(userId);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}

