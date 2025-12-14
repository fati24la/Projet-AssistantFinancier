package com.assistantfinancer.controller;

import com.assistantfinancer.dto.CourseDto;
import com.assistantfinancer.model.UserProgress;
import com.assistantfinancer.service.EducationService;
import com.assistantfinancer.service.GamificationService;
import com.assistantfinancer.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/education")
public class EducationController {

    @Autowired
    private EducationService educationService;

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private UserUtil userUtil;

    @GetMapping("/courses")
    public ResponseEntity<List<CourseDto>> getCourses(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String category) {
        List<CourseDto> courses = educationService.getCourses(language, category);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<?> getCourse(@PathVariable Long id) {
        try {
            Long userId = userUtil.getUserIdFromAuth();
            CourseDto course = educationService.getCourse(id, userId);
            return ResponseEntity.ok(course);
        } catch (RuntimeException e) {
            System.out.println("❌ [EducationController] Erreur getCourse: " + e.getMessage());
            return ResponseEntity.status(401).body("Erreur d'authentification: " + e.getMessage());
        }
    }

    @PostMapping("/courses/{id}/start")
    public ResponseEntity<?> startCourse(@PathVariable Long id) {
        try {
            System.out.println("🎓 [EducationController] Démarrage du cours ID: " + id);
            Long userId = userUtil.getUserIdFromAuth();
            System.out.println("✅ [EducationController] User ID: " + userId);
            UserProgress progress = educationService.startCourse(userId, id);
            System.out.println("✅ [EducationController] Cours démarré avec succès");
            return ResponseEntity.ok(progress);
        } catch (RuntimeException e) {
            System.out.println("❌ [EducationController] Erreur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body("Erreur d'authentification: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ [EducationController] Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur: " + e.getMessage());
        }
    }

    @PostMapping("/courses/{id}/complete")
    public ResponseEntity<?> completeCourse(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        try {
            Long userId = userUtil.getUserIdFromAuth();
            Integer score = request.get("score");
            UserProgress progress = educationService.completeCourse(userId, id, score);
            
            // Ajouter des points pour la complétion
            gamificationService.addPoints(userId, score != null ? score : 20);
            
            return ResponseEntity.ok(progress);
        } catch (RuntimeException e) {
            System.out.println("❌ [EducationController] Erreur completeCourse: " + e.getMessage());
            return ResponseEntity.status(401).body("Erreur d'authentification: " + e.getMessage());
        }
    }
}

