package com.clinica.gestor_citas.service;

import com.clinica.gestor_citas.model.Usuario;
import com.clinica.gestor_citas.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> validarLogin(String nombre, String dni, String password) {
        if (dni != null && !dni.isEmpty()) {
            return usuarioRepository.findByDniAndPassword(dni, password);
        } else if (nombre != null && !nombre.isEmpty()) {
            return usuarioRepository.findByNombreAndPassword(nombre, password);
        }
        return Optional.empty();
    }
}