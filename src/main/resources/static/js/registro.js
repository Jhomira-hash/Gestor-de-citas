document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector("form");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const usuario = {
            nombre: document.getElementById("nombre").value.trim(),
            apellido: document.getElementById("apellido").value.trim(),
            dni: document.getElementById("dni").value.trim(),
            telefono: document.getElementById("telefono").value.trim(),
            email: document.getElementById("email").value.trim(),
            password: document.getElementById("password").value.trim()
        };

        if (!usuario.nombre || !usuario.apellido || !usuario.dni || !usuario.email || !usuario.password) {
            alert("⚠️ Por favor, completa todos los campos obligatorios.");
            return;
        }

        try {
            const response = await fetch("http://localhost:8080/api/usuarios/registro", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(usuario)
            });

            const data = await response.json();

            if (response.ok && data.usuario) {
                alert("✅ Registro exitoso. ¡Ahora puedes iniciar sesión!");
                window.location.href = "login.html";
            } else {
                alert("❌ No se pudo registrar: " + (data.mensaje || "verifica tus datos"));
            }
        } catch (error) {
            console.error("Error:", error);
            alert("⚠️ No se pudo conectar con el servidor.");
        }
    });
});
