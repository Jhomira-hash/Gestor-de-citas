package com.clinica.gestor_citas.repository;

import com.clinica.gestor_citas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNombreAndPassword(String nombre, String password);
    Optional<Usuario> findByDniAndPassword(String dni, String password);

}