document.getElementById("loginForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const identificador = document.getElementById("identificador").value.trim();
    const password = document.getElementById("password").value.trim();

    // Detectar si el usuario ingresó un DNI (solo números)
    let nombre = null;
    let dni = null;

    if (/^\d+$/.test(identificador)) {
        dni = identificador;
    } else {
        nombre = identificador;
    }

    try {
        const response = await fetch("http://localhost:8080/api/usuarios/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ nombre, dni, password })
        });

        const data = await response.json();

        if (response.ok && data.usuario) {
            alert(`✅ Bienvenido, ${data.usuario.nombre}!`);
            indow.location.href = "inicio.html";
        } else {
            alert("❌ Credenciales incorrectas. Intenta nuevamente.");
        }
    } catch (error) {
        console.error("Error al conectar con el servidor:", error);
        alert("⚠️ No se pudo conectar con el servidor.");
    }
});
