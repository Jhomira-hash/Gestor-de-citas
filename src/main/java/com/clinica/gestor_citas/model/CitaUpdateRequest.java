package com.clinica.gestor_citas.model;


public class CitaUpdateRequest {
    private Long medicoId;
    private Long especialidadId;
    private Long horarioId;

    public CitaUpdateRequest(Long medicoId, Long especialidadId, Long horarioId) {
        this.medicoId = medicoId;
        this.especialidadId = especialidadId;
        this.horarioId = horarioId;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public void setMedicoId(Long medicoId) {
        this.medicoId = medicoId;
    }

    public Long getEspecialidadId() {
        return especialidadId;
    }

    public void setEspecialidadId(Long especialidadId) {
        this.especialidadId = especialidadId;
    }

    public Long getHorarioId() {
        return horarioId;
    }

    public void setHorarioId(Long horarioId) {
        this.horarioId = horarioId;
    }
}
