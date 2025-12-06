package com.assistantfinancer.controller;


import com.assistantfinancer.model.Response;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.UserRepository;
import com.assistantfinancer.service.AssistantService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;
    private final UserRepository userRepository;

    public AssistantController(AssistantService assistantService, UserRepository userRepository) {
        this.assistantService = assistantService;
        this.userRepository = userRepository;
    }

    @PostMapping("/ask")
    public Response askQuestion(@RequestParam Long userId, @RequestParam String question) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return assistantService.askQuestion(user, question);
    }
}
