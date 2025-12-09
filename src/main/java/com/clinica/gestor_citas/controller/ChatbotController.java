package com.clinica.gestor_citas.controller;

import com.clinica.gestor_citas.model.ChatRequest;
import com.clinica.gestor_citas.model.ChatResponse;
import com.clinica.gestor_citas.model.Usuario;
import com.clinica.gestor_citas.service.ChatbotService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:8085", allowCredentials = "true")
public class ChatbotController {
    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request, HttpSession session) {
        // Recuperar usuario de la sesión
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        // Pasamos el usuario (puede ser null) al servicio
        ChatResponse response = chatbotService.procesarConversacion(request.getHistory(), usuario);

        return ResponseEntity.ok(response);
    }
}