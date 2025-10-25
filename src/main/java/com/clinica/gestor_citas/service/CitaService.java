package com.clinica.gestor_citas.service;

import com.clinica.gestor_citas.model.Cita;
import com.clinica.gestor_citas.model.Medico;
import com.clinica.gestor_citas.model.Usuario;
import com.clinica.gestor_citas.repository.CitaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CitaService {
    private final CitaRepository citaRepository;

    public CitaService(CitaRepository citaRepository) {

        this.citaRepository = citaRepository;
    }

    public List<Cita> listarCitas() {

        return citaRepository.findAll();
    }

    public List<Cita> buscarPorUsuario(Usuario usuario) {
        return citaRepository.findByUsuario(usuario);
    }

    public List<Cita> buscarPorMedico(Medico medico) {
        return citaRepository.findByMedico(medico);
    }

    public Cita guardarCita(Cita cita) {
        return citaRepository.save(cita);
    }

    public void eliminarCita(Long id) {
        citaRepository.deleteById(id);
    }
}
