document.addEventListener("DOMContentLoaded", async () => {
    // Cargar datos del usuario logueado
    await cargarPerfilUsuario();

    // Inicializar lógica de selects
    inicializarSelects();

    // Configurar botón de reserva
    const btnReservar = document.getElementById("btnReservar");
    if (btnReservar) {
        btnReservar.addEventListener("click", confirmarCita);
    }
});

// --- CARGAR DATOS USUARIO ---
async function cargarPerfilUsuario() {
    try {
        const res = await fetch("http://localhost:8085/api/usuarios/perfil", {
            credentials: "include"
        });

        if (!res.ok) {
            console.warn("⚠️ No se pudo obtener el perfil del usuario.");
            return;
        }

        const usuario = await res.json();
        // Llenamos los inputs (que están disabled)
        document.getElementById("nombre").value = usuario.nombre + " " + usuario.apellido || "";
        document.getElementById("dni").value = usuario.dni || "";
        document.getElementById("telefono").value = usuario.telefono || "";
        document.getElementById("email").value = usuario.email || "";

    } catch (error) {
        console.error("Error cargando perfil:", error);
    }
}

// --- LOGICA DE SELECTS CASCADA ---
function inicializarSelects() {
    const selectEspecialidad = document.getElementById('especialidad');
    const selectMedico = document.getElementById('medico');
    const selectFecha = document.getElementById('fecha');
    const selectHora = document.getElementById('hora');

    // A. Cargar Especialidades
    fetch('http://localhost:8085/api/especialidades')
        .then(res => res.json())
        .then(data => {
            selectEspecialidad.innerHTML = '<option selected disabled value="">Seleccione una especialidad</option>';
            data.forEach(esp => {
                const option = document.createElement('option');
                option.value = esp.idEspecialidad;
                option.textContent = esp.nombre;
                selectEspecialidad.appendChild(option);
            });
        })
        .catch(error => console.error('Error cargando especialidades:', error));

    // B. Cargar Médicos al cambiar Especialidad
    selectEspecialidad.addEventListener('change', () => {
        const idEspecialidad = selectEspecialidad.value;

        // Reset selects
        selectMedico.innerHTML = '<option selected disabled value="">Seleccione un médico</option>';
        selectFecha.innerHTML = '<option selected disabled value="">Seleccione una fecha</option>';
        selectHora.innerHTML = '<option selected disabled value="">Seleccione una hora</option>';

        if(!idEspecialidad) return;

        fetch(`http://localhost:8085/api/medicos/especialidad/${idEspecialidad}`)
            .then(res => res.json())
            .then(data => {
                data.forEach(med => {
                    const option = document.createElement('option');
                    option.value = med.idMedico;
                    option.textContent = `Dr. ${med.nombre} ${med.apellido}`;
                    selectMedico.appendChild(option);
                });
            })
            .catch(error => console.error('Error cargando médicos:', error));
    });

    // C. Cargar Horarios al cambiar Médico
    selectMedico.addEventListener('change', () => {
        const idMedico = selectMedico.value;
        selectFecha.innerHTML = '<option selected disabled value="">Seleccione una fecha</option>';
        selectHora.innerHTML = '<option selected disabled value="">Seleccione una hora</option>';

        if(!idMedico) return;

        fetch(`http://localhost:8085/api/horarios/medico/${idMedico}`)
            .then(res => res.json())
            .then(data => {
                if (!data || data.length === 0) return;

                const disponibles = data.filter(h => h.disponible === true);
                const fechasUnicas = [...new Set(disponibles.map(h => h.fecha))];

                fechasUnicas.forEach(fecha => {
                    const option = document.createElement('option');
                    option.value = fecha;
                    option.textContent = fecha;
                    selectFecha.appendChild(option);
                });

                // D. Cargar Horas al seleccionar Fecha
                selectFecha.addEventListener('change', () => {
                    const fechaSeleccionada = selectFecha.value;
                    selectHora.innerHTML = '<option selected disabled value="">Seleccione una hora</option>';

                    const horas = disponibles.filter(h => h.fecha === fechaSeleccionada);
                    horas.forEach(h => {
                        const option = document.createElement('option');
                        option.value = h.hora;
                        option.textContent = h.hora.substring(0, 5); // HH:mm
                        selectHora.appendChild(option);
                    });
                });
            })
            .catch(error => console.error('Error cargando horarios:', error));
    });
}

// --- FUNCIÓN CONFIRMAR CITA (Aquí estaba el error visual) ---
function confirmarCita() {
    const nombre = document.getElementById("nombre").value;
    const dni = document.getElementById("dni").value;
    const telefono = document.getElementById("telefono").value;
    const email = document.getElementById("email").value;

    const especialidadSelect = document.getElementById("especialidad");
    const medicoSelect = document.getElementById("medico");
    const fecha = document.getElementById("fecha").value;
    const hora = document.getElementById("hora").value;

    // Validación
    if (!especialidadSelect.value || !medicoSelect.value || !fecha || !hora) {
        alert("⚠️ Por favor, selecciona Especialidad, Médico, Fecha y Hora.");
        return;
    }

    // Generar HTML del Modal
    const resumenHTML = `
    <ul class="list-group list-group-flush">
      <li class="list-group-item"><strong>👤 Paciente:</strong> ${nombre}</li>
      <li class="list-group-item"><strong>🆔 DNI:</strong> ${dni}</li>
      <li class="list-group-item"><strong>📞 Teléfono:</strong> ${telefono}</li>
      <li class="list-group-item"><strong>✉️ Correo:</strong> ${email}</li>
      <li class="list-group-item"><strong>🏥 Especialidad:</strong> ${especialidadSelect.options[especialidadSelect.selectedIndex].text}</li>
      <li class="list-group-item"><strong>🩺 Médico:</strong> ${medicoSelect.options[medicoSelect.selectedIndex].text}</li>
      <li class="list-group-item"><strong>📅 Fecha:</strong> ${fecha}</li>
      <li class="list-group-item"><strong>⏰ Hora:</strong> ${hora}</li>
    </ul>
  `;

    document.getElementById("datosCita").innerHTML = resumenHTML;

    // 4. Mostrar Modal
    const modalElement = document.getElementById("modalConfirmacion");
    const modal = new bootstrap.Modal(modalElement);
    modal.show();

    // 5. Configurar el botón "Confirmar" del modal
    const btnConfirmar = document.getElementById("btnConfirmarEnvio");

    // Limpiamos eventos anteriores para evitar duplicados
    const nuevoBtn = btnConfirmar.cloneNode(true);
    btnConfirmar.parentNode.replaceChild(nuevoBtn, btnConfirmar);

    nuevoBtn.onclick = async () => {
        await enviarReservaBackend();
        modal.hide();
    };
}

// --- ENVÍO AL BACKEND ---
async function enviarReservaBackend() {
    try {
        const especialidadId = document.getElementById("especialidad").value;
        const medicoId = document.getElementById("medico").value;
        const fecha = document.getElementById("fecha").value;
        const hora = document.getElementById("hora").value;

        // Buscar horario exacto
        const horarioRes = await fetch(
            `http://localhost:8085/api/horarios/buscar?medicoId=${medicoId}&fecha=${fecha}&hora=${hora}`,
            { credentials: "include" }
        );

        if (!horarioRes.ok) throw new Error("No se encontró el horario disponible.");
        const horario = await horarioRes.json();

        // Registrar
        const citaRes = await fetch("http://localhost:8085/api/citas", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({
                medicoId: parseInt(medicoId),
                especialidadId: parseInt(especialidadId),
                horarioId: horario.idHorario,
            }),
        });

        if (!citaRes.ok) throw new Error("Error al registrar la cita.");

        mostrarExitoModal();

    } catch (err) {
        console.error("❌ Error:", err);
        alert("Ocurrió un error: " + err.message);
    }
}

function mostrarExitoModal() {
    // Si ya existe el modal de éxito, bórralo antes de crear uno nuevo
    const existingModal = document.getElementById("modalExito");
    if (existingModal) existingModal.remove();

    const exitoModal = document.createElement("div");
    exitoModal.innerHTML = `
    <div class="modal fade" id="modalExito" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content text-center border-0 rounded-4 p-4">
          <div class="text-success fs-1 mb-3"><i class="bi bi-check-circle-fill" style="font-size: 3rem;"></i></div>
          <h5 class="fw-bold text-success">¡Cita Confirmada!</h5>
          <p class="text-secondary mb-3">Tu cita ha sido registrada correctamente.</p>
          <button class="btn btn-success w-50 mx-auto" data-bs-dismiss="modal">Aceptar</button>
        </div>
      </div>
    </div>`;
    document.body.appendChild(exitoModal);
    new bootstrap.Modal(document.getElementById("modalExito")).show();
}