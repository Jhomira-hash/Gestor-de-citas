package com.clinica.gestor_citas.controller;


import com.clinica.gestor_citas.model.Usuario;
import com.clinica.gestor_citas.service.UsuarioService;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> datosLogin) {
        String nombre = datosLogin.get("nombre");
        String dni = datosLogin.get("dni");
        String password = datosLogin.get("password");

        Optional<Usuario> usuario = usuarioService.validarLogin(nombre, dni, password);

        Map<String, Object> respuesta = new HashMap<>();
        if (usuario.isPresent()) {
            respuesta.put("mensaje", "Inicio de sesión exitoso");
            respuesta.put("usuario", usuario.get());
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


}
