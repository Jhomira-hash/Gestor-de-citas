package com.clinica.gestor_citas.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
    @Table(name = "cita")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Cita {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id_cita")
        private Long idCita;

        @ManyToOne
        @JoinColumn(name = "id_usuario", nullable = false)
        private Usuario usuario;

        @ManyToOne
        @JoinColumn(name = "id_medico", nullable = false)
        private Medico medico;

        @ManyToOne
        @JoinColumn(name = "id_especialidad", nullable = false)
        private Especialidad especialidad;

        @ManyToOne
        @JoinColumn(name = "id_horario", nullable = false)
        private Horario horario;

        @Column(name = "fecha_reserva", updatable = false, insertable = false)
        private LocalDateTime fechaReserva;
    }
