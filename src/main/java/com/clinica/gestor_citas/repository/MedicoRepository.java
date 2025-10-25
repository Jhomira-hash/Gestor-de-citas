package com.clinica.gestor_citas.repository;
import com.clinica.gestor_citas.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Long>{
    List<Medico> findByEspecialidadNombre(String nombre);
}
