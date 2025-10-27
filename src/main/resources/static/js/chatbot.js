document.addEventListener("DOMContentLoaded", () => {
  const modal = document.getElementById("chatbotModal");
  const openBtn = document.getElementById("chatbotBtn");
  const closeBtn = document.getElementById("closeChatbot");
  const sendBtn = document.getElementById("sendMsg");
  const chatInput = document.getElementById("chatInput");
  const chatContent = document.getElementById("chatContent");

  // Abrir modal
  openBtn.addEventListener("click", () => {
    modal.style.display = "block";
  });

  // Cerrar modal
  closeBtn.addEventListener("click", () => {
    modal.style.display = "none";
  });

  // Enviar mensaje
  sendBtn.addEventListener("click", sendMessage);
  chatInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter") sendMessage();
  });

  function sendMessage() {
    const msg = chatInput.value.trim();
    if (!msg) return;

    // Mensaje del usuario
    const userMsg = document.createElement("div");
    userMsg.className = "p-2 bg-purple text-white rounded ms-auto mb-2 small";
    userMsg.style.maxWidth = "80%";
    userMsg.textContent = msg;
    chatContent.appendChild(userMsg);

    chatInput.value = "";

    // Simular respuesta
    setTimeout(() => {
      const botMsg = document.createElement("div");
      botMsg.className = "p-2 bg-light rounded mb-2 text-muted small";
      botMsg.style.maxWidth = "80%";
      botMsg.textContent = "Gracias por tu mensaje, pronto te responderemos 😊";
      chatContent.appendChild(botMsg);
      chatContent.scrollTop = chatContent.scrollHeight;
    }, 800);

    chatContent.scrollTop = chatContent.scrollHeight;
  }
});
