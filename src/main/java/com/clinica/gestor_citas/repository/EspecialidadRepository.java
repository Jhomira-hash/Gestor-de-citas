package com.clinica.gestor_citas.repository;

import com.clinica.gestor_citas.model.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
}