package com.clinica.gestor_citas.repository;

import com.clinica.gestor_citas.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Long> {

    List<Horario> findByMedicoNombreAndDisponibleTrue(String nombre);

    List<Horario> findByMedicoNombreAndFechaAndDisponibleTrue(String nombre, LocalDate fecha);
}