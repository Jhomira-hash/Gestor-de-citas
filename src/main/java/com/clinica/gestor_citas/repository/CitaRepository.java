package com.clinica.gestor_citas.repository;

import com.clinica.gestor_citas.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByUsuarioIdUsuario(Long idUsuario);

}
