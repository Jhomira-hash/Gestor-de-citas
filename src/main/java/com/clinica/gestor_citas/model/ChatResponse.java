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
    private String action;
    private Long especialidadId;
    private String predictedSpecialty;

    private List<Medico> medicosSugeridos;

}
