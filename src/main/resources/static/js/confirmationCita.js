document.addEventListener("DOMContentLoaded", async () => {
    await cargarPerfilUsuario();
    inicializarSelects();
});


//  autocompletar los datos del usuario logueado
async function cargarPerfilUsuario() {
    try {
        const res = await fetch("http://localhost:8085/api/usuarios/perfil", {
            credentials: "include"
        });

        if (!res.ok) {
            console.warn("⚠️ No se pudo obtener el perfil del usuario. Código:", res.status);
            return;
        }

        const usuario = await res.json();

        // ✅ Autocompletar campos del formulario
        document.getElementById("nombre").value = usuario.nombre || "";
        document.getElementById("dni").value = usuario.dni || "";
        document.getElementById("telefono").value = usuario.telefono || "";
        document.getElementById("email").value = usuario.email || "";

    } catch (error) {
        console.error("Error cargando perfil:", error);
    }
}


// cargar especialidad, médico, fecha y hora
function inicializarSelects() {
    const selectEspecialidad = document.getElementById('especialidad');
    const selectMedico = document.getElementById('medico');
    const selectFecha = document.getElementById('fecha');
    const selectHora = document.getElementById('hora');

    // Cargar especialidades
    fetch('http://localhost:8085/api/especialidades')
        .then(res => res.json())
        .then(data => {
            data.forEach(esp => {
                const option = document.createElement('option');
                option.value = esp.idEspecialidad; // 👈 asegúrate que coincida con tu entidad
                option.textContent = esp.nombre;
                selectEspecialidad.appendChild(option);
            });
        })
        .catch(error => console.error('Error cargando especialidades:', error));


    // Cargar médicos según especialidad
    selectEspecialidad.addEventListener('change', () => {
        const idEspecialidad = selectEspecialidad.value;

        // Limpiar selects dependientes
        selectMedico.innerHTML = '<option selected disabled>Seleccione un médico</option>';
        selectFecha.innerHTML = '<option selected disabled>Seleccione una fecha</option>';
        selectHora.innerHTML = '<option selected disabled>Seleccione una hora</option>';

        fetch(`http://localhost:8085/api/medicos/especialidad/${idEspecialidad}`)
            .then(res => res.json())
            .then(data => {
                data.forEach(med => {
                    const option = document.createElement('option');
                    option.value = med.idMedico; // 👈 asegúrate que coincida con tu entidad
                    option.textContent = `${med.nombre} ${med.apellido}`;
                    selectMedico.appendChild(option);
                });
            })
            .catch(error => console.error('Error cargando médicos:', error));
    });


    // Cargar horarios según médico
    selectMedico.addEventListener('change', () => {
        const idMedico = selectMedico.value;


        selectFecha.innerHTML = '<option selected disabled>Seleccione una fecha</option>';
        selectHora.innerHTML = '<option selected disabled>Seleccione una hora</option>';

        fetch(`http://localhost:8085/api/horarios/medico/${idMedico}`)
            .then(res => res.json())
            .then(data => {
                if (!data || data.length === 0) {
                    console.warn("⚠️ No hay horarios disponibles para este médico.");
                    return;
                }

                // Filtrar solo horarios disponibles
                const disponibles = data.filter(h => h.disponible === true);
                if (disponibles.length === 0) {
                    console.warn("⚠️ Todos los horarios están ocupados.");
                    return;
                }

                // Extraer fechas únicas
                const fechasUnicas = [...new Set(disponibles.map(h => h.fecha))];
                fechasUnicas.forEach(fecha => {
                    const option = document.createElement('option');
                    option.value = fecha;
                    option.textContent = fecha;
                    selectFecha.appendChild(option);
                });

                // Cargar horas al seleccionar una fecha
                selectFecha.addEventListener('change', () => {
                    const fechaSeleccionada = selectFecha.value;
                    selectHora.innerHTML = '<option selected disabled>Seleccione una hora</option>';

                    const horas = disponibles.filter(h => h.fecha === fechaSeleccionada);
                    horas.forEach(h => {
                        const option = document.createElement('option');
                        option.value = h.hora;
                        option.textContent = h.hora.substring(0, 5); // formato HH:mm
                        selectHora.appendChild(option);
                    });
                });
            })
            .catch(error => console.error('Error cargando horarios:', error));
    });
}
document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("formCita");

    if (!form) {
        console.error("❌ No se encontró el formulario con id='formCita'");
        return;
    }

    console.log("✅ Script cargado, formulario detectado");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        console.log("🚀 Evento submit detectado correctamente");

        const especialidadId = document.querySelector("#especialidad").value;
        const medicoId = document.querySelector("#medico").value;
        const fecha = document.querySelector("#fecha").value;
        const hora = document.querySelector("#hora").value;

        if (!especialidadId || !medicoId || !fecha || !hora) {
            alert("Por favor, completa todos los campos antes de agendar la cita.");
            return;
        }

        try {
            // Buscar horario específico
            const horarioResponse = await fetch(
                `http://localhost:8085/api/horarios/buscar?medicoId=${medicoId}&fecha=${fecha}&hora=${hora}`,
                { credentials: "include" }
            );

            if (!horarioResponse.ok) throw new Error("No se pudo obtener el horario seleccionado.");

            const horario = await horarioResponse.json();
            console.log("Horario encontrado:", horario);

            // Registrar cita
            const citaResponse = await fetch("http://localhost:8085/api/citas", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({
                    medicoId: parseInt(medicoId),
                    especialidadId: parseInt(especialidadId),
                    horarioId: horario.idHorario,
                }),
            });

            if (!citaResponse.ok) throw new Error("Error al registrar la cita.");

            const citaGuardada = await citaResponse.json();
            console.log("Cita registrada:", citaGuardada);

            alert("✅ Cita registrada correctamente");
            // window.location.href = "/html/misCitas.html"; // opcional

        } catch (error) {
            console.error(error);
            alert("❌ " + error.message);
        }
    });
});




//MODALLLL DE CONFIRMACIONNN////
document.addEventListener("DOMContentLoaded", () => {
    const btnReservar = document.getElementById("btnReservar");

    if (btnReservar) {
        btnReservar.addEventListener("click", confirmarCita);
    }
});

function confirmarCita() {
    const nombre = document.getElementById("nombre").value;
    const dni = document.getElementById("dni").value;
    const telefono = document.getElementById("telefono").value;
    const email = document.getElementById("email").value;
    const especialidadSelect = document.getElementById("especialidad");
    const medicoSelect = document.getElementById("medico");
    const fecha = document.getElementById("fecha").value;
    const hora = document.getElementById("hora").value;

    if (!nombre || !dni || !telefono || !email || !especialidadSelect.value || !medicoSelect.value || !fecha || !hora) {
        alert("⚠️ Por favor, completa todos los campos antes de continuar.");
        return;
    }

    const resumenHTML = `
    <ul class="list-group list-group-flush">
      <li class="list-group-item"><strong>👤 Nombre:</strong> ${nombre}</li>
      <li class="list-group-item"><strong>🆔 DNI:</strong> ${dni}</li>
      <li class="list-group-item"><strong>📞 Teléfono:</strong> ${telefono}</li>
      <li class="list-group-item"><strong>✉️ Correo:</strong> ${email}</li>
      <li class="list-group-item"><strong>🏥 Especialidad:</strong> ${especialidadSelect.selectedOptions[0].text}</li>
      <li class="list-group-item"><strong>🩺 Médico:</strong> ${medicoSelect.selectedOptions[0].text}</li>
      <li class="list-group-item"><strong>📅 Fecha:</strong> ${fecha}</li>
      <li class="list-group-item"><strong>⏰ Hora:</strong> ${hora}</li>
    </ul>
  `;

    document.getElementById("datosCita").innerHTML = resumenHTML;

    const modal = new bootstrap.Modal(document.getElementById("modalConfirmacion"));
    modal.show();

    document.getElementById("btnConfirmarEnvio").onclick = async () => {
        await registrarCita();
        modal.hide();
    };
}

async function registrarCita() {
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

        if (!horarioRes.ok) throw new Error("No se encontró el horario.");

        const horario = await horarioRes.json();

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

        const cita = await citaRes.json();
        console.log("✅ Cita registrada:", cita);

        mostrarExitoModal();

    } catch (err) {
        console.error("❌ Error:", err);
        alert("Ocurrió un error al registrar la cita.");
    }
}

function mostrarExitoModal() {
    const exitoModal = document.createElement("div");
    exitoModal.innerHTML = `
    <div class="modal fade" id="modalExito" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content text-center border-0 rounded-4 p-4">
          <div class="text-success fs-1 mb-3">
            <i class="bi bi-check-circle-fill"></i>
          </div>
          <h5 class="fw-bold text-success">¡Cita Confirmada!</h5>
          <p class="text-secondary mb-3">Tu cita ha sido registrada correctamente.</p>
          <button class="btn btn-success w-50 mx-auto" data-bs-dismiss="modal">Aceptar</button>
        </div>
      </div>
    </div>
  `;
    document.body.appendChild(exitoModal);
    new bootstrap.Modal(document.getElementById("modalExito")).show();
}