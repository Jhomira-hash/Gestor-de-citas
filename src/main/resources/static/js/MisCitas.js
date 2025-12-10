
const usuario = JSON.parse(localStorage.getItem("usuario"));

if (!usuario) {
    alert("Debes iniciar sesión para ver tus citas.");
    window.location.href = "login.html";
}


let listaCitas = [];


async function cargarCitas() {
    const usuario = JSON.parse(localStorage.getItem("usuario"));
    if (!usuario) return;

    try {
        const response = await fetch(`http://localhost:8085/api/citas/usuario/${usuario.idUsuario}`);
        if (!response.ok) {
            console.error("Error al obtener citas:", response.status);
            return;
        }
        const citas = await response.json();
        console.log("Citas recibidas:", citas);

        // GUARDAR EN LA LISTA GLOBAL
        listaCitas = citas;

        // Mostrar citas en tabla
        mostrarCitasEnTabla(citas);

        // Actualizar resumen
        actualizarResumen(citas);

    } catch (error) {
        console.error("Error cargando citas:", error);
    }
}



function actualizarResumen(citas) {
    document.getElementById("totalCitas").textContent = citas.length;

    const hoy = new Date();
    const proximas = citas.filter(c => new Date(c.horario.fecha) >= hoy);

    document.getElementById("citasProximas").textContent = proximas.length;
}


function mostrarCitasEnTabla(citas) {
    const tbody = document.getElementById("tablaCitasBody");
    tbody.innerHTML = "";
    const hoy = new Date();

    citas.forEach((cita, index) => {

        const fecha = cita.horario?.fecha || "Sin fecha";
        const hora = cita.horario?.hora?.substring(0, 5) || "Sin hora";
        // Convertimos fecha de cita a Date para comparar
        const fechaCita = new Date(fecha + "T" + (cita.horario?.hora || "00:00:00"));

        // Solo mostrar botones si la cita es futura
        const mostrarBotones = fechaCita >= hoy;

        const fila = `
            <tr>
                <td>${cita.especialidad?.nombre || "Sin especialidad"}</td>
                <td>${cita.medico?.nombre || "Sin médico"}</td>
                <td>${fecha}</td>
                <td>${hora}</td>
                 <td>
                    ${mostrarBotones ? `<button class="btn btn-warning btn-sm" onclick="editarCita(${index})">Modificar Cita</button>` : ''}
                    ${mostrarBotones ? `<button class="btn btn-danger btn-sm" onclick="cancelarCita(${cita.idCita})">Cancelar Cita</button>` : ''}
                </td>
            </tr>
        `;

        tbody.innerHTML += fila;
    });
}

async function cancelarCita(idCita) {
    if (!confirm("¿Estás seguro de cancelar esta cita?")) return;

    try {
        const res = await fetch(`http://localhost:8085/api/citas/${idCita}`, {
            method: "DELETE"
        });

        if (!res.ok) throw new Error("No se pudo cancelar la cita.");

        alert("✅ Cita cancelada correctamente.");

        // Volver a cargar la lista de citas
        await cargarCitas();

    } catch (error) {
        console.error(error);
        alert("❌ Error al cancelar la cita.");
    }
}


async function editarCita(index) {
    index = parseInt(index, 10);

    const cita = listaCitas[index];
    if (!cita) {
        console.error("❌ Cita no encontrada");
        return;
    }

    const editIndexInput = document.getElementById("editIndex");
    editIndexInput.value = index;

    const modal = new bootstrap.Modal(document.getElementById("modalEditarCita"));
    modal.show();

    await cargarDatosParaEditar(cita);
}



async function cargarDatosParaEditar(cita) {
    const selectEspecialidad = document.getElementById('editEspecialidad');
    const selectMedico = document.getElementById('editMedico');
    const selectFecha = document.getElementById('editFecha');
    const selectHora = document.getElementById('editHora');


    const especialidadesRes = await fetch("http://localhost:8085/api/especialidades");
    const especialidades = await especialidadesRes.json();

    selectEspecialidad.innerHTML = '<option disabled>Seleccione</option>';

    especialidades.forEach(esp => {
        const op = document.createElement("option");
        op.value = esp.idEspecialidad;
        op.textContent = esp.nombre;
        if (esp.idEspecialidad === cita.especialidad.idEspecialidad) op.selected = true;
        selectEspecialidad.appendChild(op);
    });

    async function cargarMedicosPorEspecialidad(idEspecialidad, medicoSeleccionadoId = null) {
        selectMedico.innerHTML = '<option disabled>Seleccione</option>';
        selectFecha.innerHTML = '<option disabled>Seleccione</option>';
        selectHora.innerHTML = '<option disabled>Seleccione</option>';

        if (!idEspecialidad) return;

        const medicosRes = await fetch(`http://localhost:8085/api/medicos/especialidad/${idEspecialidad}`);
        const medicos = await medicosRes.json();

        medicos.forEach(med => {
            const op = document.createElement("option");
            op.value = med.idMedico;
            op.textContent = `${med.nombre} ${med.apellido}`;
            if (med.idMedico === medicoSeleccionadoId) op.selected = true;
            selectMedico.appendChild(op);
        });

        if (medicoSeleccionadoId) {
            cargarHorarios(medicoSeleccionadoId, cita.horario);
        }
    }

    async function cargarHorarios(idMedico, horarioSeleccionado = null) {
        selectFecha.innerHTML = '<option disabled>Seleccione</option>';
        selectHora.innerHTML = '<option disabled>Seleccione</option>';

        if (!idMedico) return;

        const horariosRes = await fetch(`http://localhost:8085/api/horarios/medico/${idMedico}`);
        const horarios = await horariosRes.json();

        const disponibles = horarios.filter(h => h.disponible === true || (horarioSeleccionado && h.idHorario === horarioSeleccionado.idHorario));

        const fechasUnicas = [...new Set(disponibles.map(h => h.fecha))];
        fechasUnicas.forEach(f => {
            const op = document.createElement("option");
            op.value = f;
            op.textContent = f;
            if (horarioSeleccionado && f === horarioSeleccionado.fecha) op.selected = true;
            selectFecha.appendChild(op);
        });

        selectFecha.addEventListener('change', () => {
            selectHora.innerHTML = '<option disabled>Seleccione</option>';
            const fechaSel = selectFecha.value;
            const horas = disponibles.filter(h => h.fecha === fechaSel);
            horas.forEach(h => {
                const op = document.createElement("option");
                op.value = h.hora;
                op.textContent = h.hora.substring(0, 5);
                selectHora.appendChild(op);
            });
        });

        if (horarioSeleccionado) {
            selectHora.innerHTML = '<option disabled>Seleccione</option>';
            const horas = disponibles.filter(h => h.fecha === horarioSeleccionado.fecha);
            horas.forEach(h => {
                const op = document.createElement("option");
                op.value = h.hora;
                op.textContent = h.hora.substring(0, 5);
                if (h.hora === horarioSeleccionado.hora) op.selected = true;
                selectHora.appendChild(op);
            });
        }
    }

    await cargarMedicosPorEspecialidad(cita.especialidad.idEspecialidad, cita.medico.idMedico);

    selectEspecialidad.addEventListener('change', () => {
        cargarMedicosPorEspecialidad(selectEspecialidad.value);
    });

    selectMedico.addEventListener('change', () => {
        cargarHorarios(selectMedico.value);
    });
}

document.getElementById("formEditarCita").addEventListener("submit", async (e) => {
    e.preventDefault();

    const index = document.getElementById("editIndex").value;
    const cita = listaCitas[index];

    const especialidadId = document.getElementById("editEspecialidad").value;
    const medicoId = document.getElementById("editMedico").value;
    const fecha = document.getElementById("editFecha").value;
    const hora = document.getElementById("editHora").value;

    if (!especialidadId || !medicoId || !fecha || !hora) {
        alert("⚠️ Completa todos los campos antes de guardar.");
        return;
    }

    try {
        // 1️⃣ Obtener el horario correspondiente al médico, fecha y hora
        const horarioRes = await fetch(
            `http://localhost:8085/api/horarios/buscar?medicoId=${medicoId}&fecha=${fecha}&hora=${hora}`
        );
        if (!horarioRes.ok) throw new Error("No se encontró el horario seleccionado.");
        const horario = await horarioRes.json();

        // 2️⃣ Enviar cambios al backend

        const res = await fetch(`http://localhost:8085/api/citas/${cita.idCita}`, {
            method: "PUT", // o PATCH según tu API
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                medicoId: parseInt(medicoId),
                especialidadId: parseInt(especialidadId),
                horarioId: horario.idHorario
            }),
        });

        if (!res.ok) throw new Error("Error al guardar los cambios.");

        const citaActualizada = await res.json();
        console.log("Cita actualizada:", citaActualizada);

        alert("✅ Cambios guardados correctamente");

        listaCitas[index] = citaActualizada;
        mostrarCitasEnTabla(listaCitas);

        const modal = bootstrap.Modal.getInstance(document.getElementById("modalEditarCita"));
        modal.hide();

    } catch (error) {
        console.error("Error al actualizar cita:", error);
        alert("❌ No se pudieron guardar los cambios.");
    }
});


cargarCitas();