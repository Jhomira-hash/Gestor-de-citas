package com.clinica.gestor_citas.controller;

import com.clinica.gestor_citas.model.Especialidad;
import com.clinica.gestor_citas.model.Horario;
import com.clinica.gestor_citas.service.EspecialidadService;
import com.clinica.gestor_citas.service.HorarioService;
import com.clinica.gestor_citas.service.MedicoService;
import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prueba")
@CrossOrigin(origins = "http://localhost:8085", allowCredentials = "true")
public class PruebaIAController {

    private final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final String API_KEY_GROQ = "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";

    private final EspecialidadService especialidadService;
    private final MedicoService medicoService;
    private final HorarioService horarioService;

    public PruebaIAController(EspecialidadService especialidadService,
                              MedicoService medicoService,
                              HorarioService horarioService) {
        this.especialidadService = especialidadService;
        this.medicoService = medicoService;
        this.horarioService = horarioService;
    }

    @PostMapping("/chat1")
    public ResponseEntity<Map<String, String>> chatWithGroq(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("response", "Error: mensaje vacío"));
        }

        try {
            // Cargar especialidades
            List<Especialidad> especialidades = especialidadService.listarEspecialidades();
            String especialidadesTexto = especialidades.stream()
                    .map(Especialidad::getNombre)
                    .collect(Collectors.joining(", "));

            // Cargar médicos agrupados por especialidad
            Map<String, List<String>> medicosPorEspecialidad = medicoService.listarMedicos().stream()
                    .collect(Collectors.groupingBy(
                            m -> m.getEspecialidad().getNombre(),
                            Collectors.mapping(
                                    m -> "Dr(a). " + m.getNombre() + " " + m.getApellido(),
                                    Collectors.toList()
                            )
                    ));

            String medicosTexto = medicosPorEspecialidad.entrySet().stream()
                    .map(e -> e.getKey() + ": " + String.join(", ", e.getValue()))
                    .collect(Collectors.joining("; "));

            // Contar horarios disponibles
            long horariosDisponibles = horarioService.listarTodos().stream()
                    .filter(Horario::getDisponible)
                    .count();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(API_KEY_GROQ);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "llama-3.3-70b-versatile");
            body.put("max_tokens", 500);

            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content",
                            "Eres asistente de Clínica San Martín, Ica. " +
                                    "Especialidades: " + especialidadesTexto + ". " +
                                    "Médicos disponibles: " + medicosTexto + ". " +
                                    "Hay " + horariosDisponibles + " horarios disponibles. " +
                                    "Recomienda especialidad y médico según síntomas. Sé cordial y breve."),
                    Map.of("role", "user", "content", userMessage)
            );
            body.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    GROQ_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> bodyResponse = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) bodyResponse.get("choices");

                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String reply = (String) message.get("content");
                    return ResponseEntity.ok(Collections.singletonMap("response", reply));
                }
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("response", "Error: no se recibió respuesta del modelo."));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("response", "Error: " + e.getMessage()));
        }
    }
}
