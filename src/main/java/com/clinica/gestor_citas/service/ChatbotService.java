package com.clinica.gestor_citas.service;

import com.clinica.gestor_citas.model.CitaExtraida;
import com.clinica.gestor_citas.model.Especialidad;
import com.clinica.gestor_citas.model.Horario;
import com.clinica.gestor_citas.model.Medico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatbotService {

    private final GroqChatService groqChatService;
    private final CitaService citaService;
    private final MedicoService medicoService;
    private final HorarioService horarioService;
    private final EspecialidadService especialidadService;
    public ChatbotService(GroqChatService groqChatService,
                          CitaService citaService,
                          MedicoService medicoService,
                          HorarioService horarioService,
                          EspecialidadService especialidadService){
        this.groqChatService=groqChatService;
        this.citaService=citaService;
        this.horarioService=horarioService;
        this.medicoService=medicoService;
        this.especialidadService=especialidadService;
    }

    public String procesarMensaje(String mensajeUsuario, Long usuarioId){
        try {

            //extrae datos
            String promt =  """
                    Analiza este mensaje y devuelve un JSON con los campos:
                    { "especialidad": "...", "fecha": "YYYY-MM-DD", "hora": "HH:mm" }.
                    Mensaje: "%s"
                    """.formatted(mensajeUsuario);
            String respuestaGroq = groqChatService.sendMessageToGroq(promt);
            //convertir a objeto
            CitaExtraida  citaExtraida = groqChatService.parsearCitaDesdeTexto(respuestaGroq);
            if (citaExtraida == null) {
                return "No logré entender la especialidad o la fecha de tu cita. ¿Podrías repetirlo?";
            }
            //buscar en la bdd
            Especialidad especialidad = especialidadService
                    .buscarPorNombre(citaExtraida.getEspecialidad())
                    .orElse(null);

            if (especialidad == null) {
                return "No encontré la especialidad " + citaExtraida.getEspecialidad() + " en el sistema.";
            }
            List<Medico> medicos = medicoService.listarMedicosPorEspecialidad(especialidad.getIdEspecialidad());
            if (medicos.isEmpty()) {
                return "No hay médicos disponibles para " + especialidad.getNombre();
            }
            Medico medico = medicos.get(0);

            //buscar horario
            Optional<Horario> horarioOpt = horarioService.buscarHorario(
                    medico.getIdMedico(),
                    citaExtraida.getFecha(),
                    citaExtraida.getHora()
            );
            if (horarioOpt.isEmpty()) {
                return "No hay horario disponible para el " + citaExtraida.getFecha() +
                        " a las " + citaExtraida.getHora();
            }

            Horario horario = horarioOpt.get();


            //registrar cita

            citaService.registrarCita(usuarioId, medico.getIdMedico(),
                    especialidad.getIdEspecialidad(), horario.getIdHorario());

            return "He reservado tu cita con el Dr. " + medico.getNombre() +
                    " (" + especialidad.getNombre() + ") para el " +
                    citaExtraida.getFecha() + " a las " + citaExtraida.getHora() + ".";

        } catch (Exception e) {
            e.printStackTrace();
            return "Ocurrió un error al procesar tu solicitud: " + e.getMessage();
        }
    }
}
