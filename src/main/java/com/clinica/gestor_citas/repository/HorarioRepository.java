package com.clinica.gestor_citas.repository;

import com.clinica.gestor_citas.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {
    List<Horario> findByMedico_IdMedicoAndDisponibleTrue(Long medicoId);
    Optional<Horario> findByMedico_IdMedicoAndFechaAndHora(Long medicoId, LocalDate fecha, LocalTime hora);

}