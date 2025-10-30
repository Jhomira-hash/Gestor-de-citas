const API_BASE = "http://localhost:8080/api";

// Simular usuario autenticado (puedes reemplazar con datos reales)
const usuario = {
    id_usuario: 1,
    nombre: "María López",
    dni: "87654321"
};

// Cargar datos del usuario
document.getElementById("nombreUsuario").value = usuario.nombre;
document.getElementById("dniUsuario").value = usuario.dni;

// Cargar especialidades
async function cargarEspecialidades() {
    const res = await fetch(`${API_BASE}/especialidades`);
    const data = await res.json();

    const select = document.getElementById("especialidadSelect");
    data.forEach(e => {
        const option = document.createElement("option");
        option.value = e.id_especialidad;
        option.textContent = e.nombre;
        select.appendChild(option);
    });
}
cargarEspecialidades();

// Al cambiar especialidad → cargar médicos
document.getElementById("especialidadSelect").addEventListener("change", async (e) => {
    const idEspecialidad = e.target.value;
    const medicoSelect = document.getElementById("medicoSelect");
    medicoSelect.innerHTML = '<option value="">Seleccione un médico</option>';

    if (idEspecialidad) {
        const res = await fetch(`${API_BASE}/medicos/especialidad/${idEspecialidad}`);
        const data = await res.json();

        data.forEach(m => {
            const option = document.createElement("option");
            option.value = m.id_medico;
            option.textContent = `${m.nombre} ${m.apellido}`;
            medicoSelect.appendChild(option);
        });

        medicoSelect.disabled = false;
    } else {
        medicoSelect.disabled = true;
    }
});

// Al cambiar médico → cargar horarios disponibles
document.getElementById("medicoSelect").addEventListener("change", async (e) => {
    const idMedico = e.target.value;
    const horarioSelect = document.getElementById("horarioSelect");
    horarioSelect.innerHTML = '<option value="">Seleccione un horario</option>';

    if (idMedico) {
        const res = await fetch(`${API_BASE}/horarios/medico/${idMedico}`);
        const data = await res.json();

        data.filter(h => h.disponible).forEach(h => {
            const option = document.createElement("option");
            option.value = h.id_horario;
            option.textContent = `${h.fecha} - ${h.hora}`;
            horarioSelect.appendChild(option);
        });

        horarioSelect.disabled = false;
    } else {
        horarioSelect.disabled = true;
    }
});

// Enviar cita al backend
document.getElementById("formCita").addEventListener("submit", async (e) => {
    e.preventDefault();

    const idEspecialidad = document.getElementById("especialidadSelect").value;
    const idMedico = document.getElementById("medicoSelect").value;
    const idHorario = document.getElementById("horarioSelect").value;

    const cita = {
        id_usuario: usuario.id_usuario,
        id_especialidad: idEspecialidad,
        id_medico: idMedico,
        id_horario: idHorario
    };

    const res = await fetch(`${API_BASE}/citas`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(cita)
    });

    const data = await res.json();
    document.getElementById("mensajeCita").innerText = data.mensaje || "Cita registrada con éxito.";

    // Mostrar modal de confirmación
    const modal = new bootstrap.Modal(document.getElementById("modalConfirmacion"));
    modal.show();
});
