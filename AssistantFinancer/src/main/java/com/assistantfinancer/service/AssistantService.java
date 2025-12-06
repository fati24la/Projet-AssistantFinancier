package com.assistantfinancer.service;

import com.assistantfinancer.model.Response;
import com.assistantfinancer.model.User;

public interface AssistantService {
    Response askQuestion(User user, String questionText);
}
