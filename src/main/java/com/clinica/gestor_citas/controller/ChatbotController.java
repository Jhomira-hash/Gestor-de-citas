package com.clinica.gestor_citas.controller;

import com.clinica.gestor_citas.model.ChatRequest;
import com.clinica.gestor_citas.model.ChatResponse;
import com.clinica.gestor_citas.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        // Generar conversationId si no existe
        if (request.getConversationId() == null || request.getConversationId().isEmpty()) {
            request.setConversationId(UUID.randomUUID().toString());
        }

        ChatResponse response = chatbotService.procesarMensaje(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/iniciar")
    public ResponseEntity<ChatResponse> iniciarConversacion() {
        ChatResponse response = new ChatResponse();
        response.setConversationId(UUID.randomUUID().toString());
        response.setMessage(
                "¡Hola! 👋 Soy tu asistente médico virtual. " +
                        "¿En qué puedo ayudarte hoy? Puedo ayudarte a:\n\n" +
                        "• Recomendarte una especialidad según tus síntomas\n" +
                        "• Agendar una cita médica\n" +
                        "• Ver horarios disponibles"
        );
        return ResponseEntity.ok(response);
    }

}