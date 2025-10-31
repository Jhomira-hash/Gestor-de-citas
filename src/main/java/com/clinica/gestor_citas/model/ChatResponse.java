package com.clinica.gestor_citas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String message;
    private String conversationId;
    private CitaExtraida citaExtraida;
    private List<String> especialidadesSugeridas;
    private List<Medico> medicosSugeridos;
    private Cita citaCreada; // Si se creó una cita

}
