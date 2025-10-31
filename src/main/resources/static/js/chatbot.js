document.addEventListener("DOMContentLoaded", () => {
  const modal = document.getElementById("chatbotModal");
  const openBtn = document.getElementById("chatbotBtn");
  const closeBtn = document.getElementById("closeChatbot");
  const sendBtn = document.getElementById("sendMsg");
  const chatInput = document.getElementById("chatInput");
  const chatContent = document.getElementById("chatContent");


  function sendMessage() {
    const msg = chatInput.value.trim();
    if (!msg) return;

    // Mostrar mensaje del usuario
    const userMsg = document.createElement("div");
    userMsg.className = "p-2 bg-purple text-white rounded ms-auto mb-2 small";
    userMsg.style.maxWidth = "80%";
    userMsg.textContent = msg;
    chatContent.appendChild(userMsg);
    chatInput.value = "";

    chatContent.scrollTop = chatContent.scrollHeight;

    //  Llamar al backend Spring Boot
     fetch("http://localhost:8085/api/prueba/chat1", {
       method: "POST",
       headers: { "Content-Type": "application/json" },
       body: JSON.stringify({ message: msg })
     })
        .then(response => {
          if (!response.ok) throw new Error("Error HTTP: " + response.status);
          return response.json();
        })
    .then(data => {
      const botMsg = document.createElement("div");
      botMsg.className = "p-2 bg-light rounded mb-2 text-muted small";
      botMsg.style.maxWidth = "80%";
      botMsg.textContent = data.response || data.respuesta || "El servidor no envió una respuesta 😅"; //CAMBIO DE KARLA

      chatContent.appendChild(botMsg);
      chatContent.scrollTop = chatContent.scrollHeight;
    })
    .catch(error => {
      console.error("Error al conectar con el backend:", error);
      const errorMsg = document.createElement("div");
      errorMsg.className = "p-2 bg-danger text-white rounded mb-2 small";
      errorMsg.textContent = "Error al conectar con el servidor 😞";
      chatContent.appendChild(errorMsg);
    });
  }

  // Abrir modal
  openBtn.addEventListener("click", () => {
    modal.style.display = "block";
  });

  // Cerrar modal
  closeBtn.addEventListener("click", () => {
    modal.style.display = "none";
  });

  // Enviar mensaje (click o Enter)
  sendBtn.addEventListener("click", sendMessage);
  chatInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter") sendMessage();
  });
});
