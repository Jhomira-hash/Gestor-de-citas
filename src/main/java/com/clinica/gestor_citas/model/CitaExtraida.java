package com.clinica.gestor_citas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaExtraida {
    private String especialidad;
    private String nombreMedico;
    private LocalDate fecha;
    private LocalTime hora;


}
