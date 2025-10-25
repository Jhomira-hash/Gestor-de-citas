package com.clinica.gestor_citas.controller;

import com.clinica.gestor_citas.service.DialogflowService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final DialogflowService dialogflowService;

    public ChatbotController(DialogflowService dialogflowService) {
        this.dialogflowService = dialogflowService;
    }

    @PostMapping("/preguntar")
    public String preguntar(@RequestBody String mensaje) throws Exception {
        return dialogflowService.enviarMensaje(mensaje);
    }
}