package com.clinica.gestor_citas.service;

import com.clinica.gestor_citas.model.CitaExtraida;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class GroqChatService {

    private final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final String API_KEY_GROQ = "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";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String sendMessageToGroq(String userMessage) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(API_KEY_GROQ);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "openai/gpt-oss-20b");
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content",
                        "Eres un asistente médico que responde con precisión y puede analizar solicitudes de citas."),
                Map.of("role", "user", "content", userMessage)
        );
        body.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(GROQ_URL, HttpMethod.POST, entity, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    public CitaExtraida parsearCitaDesdeTexto(String jsonText) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonText);
            String doctor = jsonNode.get("doctor").asText();
            String especialidad = jsonNode.get("especialidad").asText();
            LocalDate fecha = LocalDate.parse(jsonNode.get("fecha").asText());
            LocalTime hora = LocalTime.parse(jsonNode.get("hora").asText());
            return new CitaExtraida(especialidad, doctor, fecha, hora);
        } catch (Exception e) {
            return null;
        }
    }
}
