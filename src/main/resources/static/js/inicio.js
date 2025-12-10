document.addEventListener("DOMContentLoaded", () => {
    const usuario = JSON.parse(localStorage.getItem("usuario"));

    const btnLogin = document.querySelector("#btnLogin");
    const menuVerCitas = document.querySelector("#menuVerCitas");

    if (!usuario) {
        console.log("No hay usuario logeado.");

        // Mostrar botón Login
        if (btnLogin) {
            btnLogin.textContent = "Iniciar sesión";
            btnLogin.style.display = "block";
            btnLogin.href = "login.html";
        }

        if (menuVerCitas) menuVerCitas.style.display = "none";

        return;

    }

    console.log("Usuario logeado:", usuario);

    if (menuVerCitas) menuVerCitas.style.display = "block";

    if (btnLogin) {
        btnLogin.textContent = "Cerrar sesión";
        btnLogin.style.display = "block";
        btnLogin.href = "#"; // Prevenir navegación
        btnLogin.addEventListener("click", () => {
            localStorage.removeItem("usuario"); // Elimina el usuario
            location.reload(); // Recargar la página
        });
    }
});
