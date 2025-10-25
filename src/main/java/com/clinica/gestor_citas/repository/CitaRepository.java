package com.clinica.gestor_citas.repository;

import com.clinica.gestor_citas.model.Cita;
import com.clinica.gestor_citas.model.Medico;
import com.clinica.gestor_citas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CitaRepository extends JpaRepository<Cita,Long> {
    List<Cita> findByUsuario(Usuario usuario);
    List<Cita> findByMedico(Medico medico);
}
