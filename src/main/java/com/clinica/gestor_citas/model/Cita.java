package com.clinica.gestor_citas.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
    @Table(name = "cita")
    //@Data
    //@NoArgsConstructor
    //@AllArgsConstructor
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

    public Cita() {
    }

    public Cita(Long idCita, Usuario usuario, Medico medico, Especialidad especialidad, Horario horario, LocalDateTime fechaReserva) {
        this.idCita = idCita;
        this.usuario = usuario;
        this.medico = medico;
        this.especialidad = especialidad;
        this.horario = horario;
        this.fechaReserva = fechaReserva;
    }

    public Long getIdCita() {
        return idCita;
    }

    public void setIdCita(Long idCita) {
        this.idCita = idCita;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public Especialidad getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(Especialidad especialidad) {
        this.especialidad = especialidad;
    }

    public Horario getHorario() {
        return horario;
    }

    public void setHorario(Horario horario) {
        this.horario = horario;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }
}
