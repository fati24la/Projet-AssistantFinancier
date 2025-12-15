package com.assistantfinancer.controller;

import com.assistantfinancer.dto.DashboardStatisticsDto;
import com.assistantfinancer.model.UserProgress;
import com.assistantfinancer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatisticsDto> getDashboardStatistics() {
        DashboardStatisticsDto stats = new DashboardStatisticsDto();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalBudgets(budgetRepository.count());
        stats.setTotalExpenses(expenseRepository.count());
        stats.setTotalCourses(courseRepository.count());
        stats.setCompletedCourses(userProgressRepository.findAll().stream()
                .filter(UserProgress::isCompleted)
                .count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/user-evolution")
    public ResponseEntity<List<Map<String, Object>>> getUserEvolution() {
        // Retourner l'évolution des utilisateurs par date
        // Pour simplifier, on retourne les 30 derniers jours
        List<Map<String, Object>> evolution = new ArrayList<>();
        long totalUsers = userRepository.count();

        for (int i = 29; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            // Approximation : on divise le total par 30 pour simuler une croissance
            dayData.put("count", (totalUsers * (30 - i)) / 30);
            evolution.add(dayData);
        }

        return ResponseEntity.ok(evolution);
    }

    @GetMapping("/budget-categories")
    public ResponseEntity<List<Map<String, Object>>> getBudgetCategoryDistribution() {
        Map<String, Long> categoryCount = budgetRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        budget -> budget.getCategory() != null ? budget.getCategory() : "OTHER",
                        Collectors.counting()
                ));

        List<Map<String, Object>> distribution = categoryCount.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("category", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/top-users")
    public ResponseEntity<List<Map<String, Object>>> getTopUsers(@RequestParam(defaultValue = "5") int limit) {
        List<Map<String, Object>> topUsers = userProfileRepository.findAll().stream()
                .filter(profile -> profile.getPoints() != null && profile.getPoints() > 0)
                .sorted((p1, p2) -> Integer.compare(p2.getPoints(), p1.getPoints()))
                .limit(limit)
                .map(profile -> {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("username", profile.getUser().getUsername());
                    userData.put("points", profile.getPoints());
                    return userData;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(topUsers);
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Map<String, Object>>> getCourseStatistics() {
        List<Map<String, Object>> courseStats = courseRepository.findAll().stream()
                .map(course -> {
                    List<UserProgress> progressList = userProgressRepository.findAll().stream()
                            .filter(up -> up.getCourse().getId().equals(course.getId()))
                            .collect(Collectors.toList());

                    long completions = progressList.stream()
                            .filter(UserProgress::isCompleted)
                            .count();

                    double averageScore = progressList.stream()
                            .filter(up -> up.getScore() != null)
                            .mapToInt(UserProgress::getScore)
                            .average()
                            .orElse(0.0);

                    Map<String, Object> stats = new HashMap<>();
                    stats.put("courseId", course.getId());
                    stats.put("courseTitle", course.getTitle());
                    stats.put("completions", completions);
                    stats.put("averageScore", averageScore);
                    return stats;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(courseStats);
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportData(@RequestParam String format) {
        try {
            if ("csv".equalsIgnoreCase(format)) {
                return exportCsv();
            } else if ("pdf".equalsIgnoreCase(format)) {
                return exportPdf();
            } else {
                return ResponseEntity.badRequest().body("Format non supporté. Utilisez 'csv' ou 'pdf'.");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'export: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur lors de l'export: " + e.getMessage());
        }
    }

    private ResponseEntity<byte[]> exportCsv() {
        StringBuilder csv = new StringBuilder();
        
        // En-têtes CSV
        csv.append("Type,Donnée,Valeur\n");
        
        // Statistiques globales
        csv.append("Global,Total Utilisateurs,").append(userRepository.count()).append("\n");
        csv.append("Global,Total Budgets,").append(budgetRepository.count()).append("\n");
        csv.append("Global,Total Dépenses,").append(expenseRepository.count()).append("\n");
        csv.append("Global,Total Cours,").append(courseRepository.count()).append("\n");
        csv.append("Global,Cours Complétés,").append(userProgressRepository.findAll().stream()
                .filter(UserProgress::isCompleted)
                .count()).append("\n");
        
        // Statistiques par cours
        csv.append("\nCours,Titre,Complétions,Score Moyen\n");
        courseRepository.findAll().forEach(course -> {
            List<UserProgress> progressList = userProgressRepository.findAll().stream()
                    .filter(up -> up.getCourse().getId().equals(course.getId()))
                    .collect(Collectors.toList());
            
            long completions = progressList.stream()
                    .filter(UserProgress::isCompleted)
                    .count();
            
            double averageScore = progressList.stream()
                    .filter(up -> up.getScore() != null)
                    .mapToInt(UserProgress::getScore)
                    .average()
                    .orElse(0.0);
            
            csv.append("Cours,")
                .append("\"").append(course.getTitle().replace("\"", "\"\"")).append("\",")
                .append(completions).append(",")
                .append(String.format("%.2f", averageScore)).append("\n");
        });
        
        // Top utilisateurs
        csv.append("\nTop Utilisateurs,Username,Points\n");
        userProfileRepository.findAll().stream()
                .filter(profile -> profile.getPoints() != null && profile.getPoints() > 0)
                .sorted((p1, p2) -> Integer.compare(p2.getPoints(), p1.getPoints()))
                .limit(10)
                .forEach(profile -> {
                    csv.append("Top User,")
                        .append(profile.getUser().getUsername()).append(",")
                        .append(profile.getPoints()).append("\n");
                });
        
        // Distribution des budgets par catégorie
        csv.append("\nBudget Catégorie,Catégorie,Nombre\n");
        budgetRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        budget -> budget.getCategory() != null ? budget.getCategory() : "OTHER",
                        Collectors.counting()
                ))
                .forEach((category, count) -> {
                    csv.append("Budget Catégorie,")
                        .append(category).append(",")
                        .append(count).append("\n");
                });
        
        byte[] csvBytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "statistics_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv");
        headers.setContentLength(csvBytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    private ResponseEntity<byte[]> exportPdf() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Police - Utiliser les polices standard d'iText 7
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            
            // Titre
            Paragraph title = new Paragraph("Statistiques détaillées")
                    .setFont(fontBold)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(title);
            
            Paragraph date = new Paragraph("Date: " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(date);
            
            // Statistiques globales
            Paragraph globalTitle = new Paragraph("Statistiques globales")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setMarginTop(15)
                    .setMarginBottom(10);
            document.add(globalTitle);
            
            Table globalTable = new Table(UnitValue.createPercentArray(new float[]{3, 2})).useAllAvailableWidth();
            globalTable.addHeaderCell(new Cell().add(new Paragraph("Type").setFont(fontBold)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            globalTable.addHeaderCell(new Cell().add(new Paragraph("Valeur").setFont(fontBold)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            
            globalTable.addCell(new Cell().add(new Paragraph("Total Utilisateurs").setFont(font)));
            globalTable.addCell(new Cell().add(new Paragraph(String.valueOf(userRepository.count())).setFont(font)));
            
            globalTable.addCell(new Cell().add(new Paragraph("Total Budgets").setFont(font)));
            globalTable.addCell(new Cell().add(new Paragraph(String.valueOf(budgetRepository.count())).setFont(font)));
            
            globalTable.addCell(new Cell().add(new Paragraph("Total Dépenses").setFont(font)));
            globalTable.addCell(new Cell().add(new Paragraph(String.valueOf(expenseRepository.count())).setFont(font)));
            
            globalTable.addCell(new Cell().add(new Paragraph("Total Cours").setFont(font)));
            globalTable.addCell(new Cell().add(new Paragraph(String.valueOf(courseRepository.count())).setFont(font)));
            
            long completedCourses = userProgressRepository.findAll().stream()
                    .filter(UserProgress::isCompleted)
                    .count();
            globalTable.addCell(new Cell().add(new Paragraph("Cours Complétés").setFont(font)));
            globalTable.addCell(new Cell().add(new Paragraph(String.valueOf(completedCourses)).setFont(font)));
            
            document.add(globalTable);
            
            // Statistiques par cours
            Paragraph courseTitle = new Paragraph("Statistiques par cours")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(courseTitle);
            
            Table courseTable = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2})).useAllAvailableWidth();
            courseTable.addHeaderCell(new Cell().add(new Paragraph("Cours").setFont(fontBold)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            courseTable.addHeaderCell(new Cell().add(new Paragraph("Complétions").setFont(fontBold)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            courseTable.addHeaderCell(new Cell().add(new Paragraph("Score Moyen (%)").setFont(fontBold)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            
            courseRepository.findAll().forEach(course -> {
                List<UserProgress> progressList = userProgressRepository.findAll().stream()
                        .filter(up -> up.getCourse().getId().equals(course.getId()))
                        .collect(Collectors.toList());
                
                long completions = progressList.stream()
                        .filter(UserProgress::isCompleted)
                        .count();
                
                double averageScore = progressList.stream()
                        .filter(up -> up.getScore() != null)
                        .mapToInt(UserProgress::getScore)
                        .average()
                        .orElse(0.0);
                
                courseTable.addCell(new Cell().add(new Paragraph(course.getTitle()).setFont(font)));
                courseTable.addCell(new Cell().add(new Paragraph(String.valueOf(completions)).setFont(font)));
                courseTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", averageScore)).setFont(font)));
            });
            
            document.add(courseTable);
            
            // Top utilisateurs
            Paragraph topUsersTitle = new Paragraph("Top 10 Utilisateurs")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(topUsersTitle);
            
            Table topUsersTable = new Table(UnitValue.createPercentArray(new float[]{3, 2})).useAllAvailableWidth();
            topUsersTable.addHeaderCell(new Cell().add(new Paragraph("Username").setFont(fontBold)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            topUsersTable.addHeaderCell(new Cell().add(new Paragraph("Points").setFont(fontBold)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            
            userProfileRepository.findAll().stream()
                    .filter(profile -> profile.getPoints() != null && profile.getPoints() > 0)
                    .sorted((p1, p2) -> Integer.compare(p2.getPoints(), p1.getPoints()))
                    .limit(10)
                    .forEach(profile -> {
                        topUsersTable.addCell(new Cell().add(new Paragraph(profile.getUser().getUsername()).setFont(font)));
                        topUsersTable.addCell(new Cell().add(new Paragraph(String.valueOf(profile.getPoints())).setFont(font)));
                    });
            
            document.add(topUsersTable);
            
            // Distribution des budgets par catégorie
            Paragraph budgetTitle = new Paragraph("Distribution des budgets par catégorie")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(budgetTitle);
            
            Table budgetTable = new Table(UnitValue.createPercentArray(new float[]{3, 2})).useAllAvailableWidth();
            budgetTable.addHeaderCell(new Cell().add(new Paragraph("Catégorie").setFont(fontBold)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            budgetTable.addHeaderCell(new Cell().add(new Paragraph("Nombre").setFont(fontBold)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            
            budgetRepository.findAll().stream()
                    .collect(Collectors.groupingBy(
                            budget -> budget.getCategory() != null ? budget.getCategory() : "OTHER",
                            Collectors.counting()
                    ))
                    .forEach((category, count) -> {
                        budgetTable.addCell(new Cell().add(new Paragraph(category).setFont(font)));
                        budgetTable.addCell(new Cell().add(new Paragraph(String.valueOf(count)).setFont(font)));
                    });
            
            document.add(budgetTable);
            
            document.close();
            
            byte[] pdfBytes = baos.toByteArray();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "statistics_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du PDF: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        }
    }
}

