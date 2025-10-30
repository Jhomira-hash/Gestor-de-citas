package com.clinica.gestor_citas.controller;


import com.clinica.gestor_citas.model.Usuario;
import com.clinica.gestor_citas.repository.UsuarioRepository;
import com.clinica.gestor_citas.service.UsuarioService;

import java.util.ArrayList;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }


    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> datosLogin, HttpServletRequest request) {
        String nombre = datosLogin.get("nombre");
        String dni = datosLogin.get("dni");
        String password = datosLogin.get("password");

        Optional<Usuario> usuarioOpt = usuarioService.validarLogin(nombre, dni, password);
        Map<String, Object> respuesta = new HashMap<>();

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    usuario.getNombre(), null, new ArrayList<>()
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            session.setAttribute("usuario", usuario);

            System.out.println(" Usuario guardado en sesión: " + usuario.getNombre());

            respuesta.put("mensaje", "Inicio de sesión exitoso");
            respuesta.put("usuario", usuario);
        } else {
            respuesta.put("mensaje", "Datos incorrectos");
        }

        return respuesta;
    }


    @PostMapping("/registro")
    public Map<String, Object> registrarUsuario(@RequestBody Usuario nuevoUsuario) {
        Map<String, Object> respuesta = new HashMap<>();

        try {
            Usuario usuarioGuardado = usuarioService.registrarUsuario(nuevoUsuario);
            respuesta.put("mensaje", "Usuario registrado exitosamente");
            respuesta.put("usuario", usuarioGuardado);
        } catch (Exception e) {
            respuesta.put("mensaje", "Error al registrar usuario: " + e.getMessage());
        }

        return respuesta;
    }
    @GetMapping("/perfil")
    public ResponseEntity<?> obtenerPerfil(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(403).body("No hay usuario en sesión");
        }
        return ResponseEntity.ok(usuario);
    }

}
