
const usuario = JSON.parse(localStorage.getItem("usuario"));

if (!usuario) {
    alert("Debes iniciar sesión para ver tu perfil.");
    window.location.href = "login.html";
}

window.addEventListener("DOMContentLoaded", () => {
    document.getElementById("nombre").value = usuario.nombre || "";
    document.getElementById("apellido").value = usuario.apellido || "";
    document.getElementById("dni").value = usuario.dni || "";
    document.getElementById("telefono").value = usuario.telefono || "";
    document.getElementById("email").value = usuario.email || "";
    document.getElementById("fechaRegistro").value = usuario.fechaRegistro
        ? new Date(usuario.fechaRegistro).toLocaleDateString()
        : "";

    document.getElementById("telefono").disabled = false;
    document.getElementById("email").disabled = false;
});


document.getElementById("formPerfil").addEventListener("submit", async (e) => {
    e.preventDefault();

    const telefono = document.getElementById("telefono").value.trim();
    const email = document.getElementById("email").value.trim();

    if (!telefono || !email) {
        alert("⚠️ Debes completar teléfono y correo.");
        return;
    }

    try {
        const res = await fetch(`http://localhost:8085/api/usuarios/${usuario.idUsuario}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ telefono, email }),
        });

        if (!res.ok) throw new Error("Error al actualizar perfil.");

        const usuarioActualizado = await res.json();
        console.log("Usuario actualizado:", usuarioActualizado);

        // Actualizar localStorage SIN borrar la sesión
        localStorage.setItem("usuario", JSON.stringify(usuarioActualizado));

        alert("✅ Perfil actualizado correctamente.");

    } catch (error) {
        console.error(error);
        alert("❌ No se pudo actualizar el perfil.");
    }
});
