document.addEventListener("DOMContentLoaded", () => {
  const modal = document.getElementById("chatbotModal");
  const openBtn = document.getElementById("chatbotBtn");
  const closeBtn = document.getElementById("closeChatbot");
  const resetBtn = document.getElementById("resetChat");
  const sendBtn = document.getElementById("sendMsg");
  const chatInput = document.getElementById("chatInput");
  const chatContent = document.getElementById("chatContent");

  let conversationHistory = JSON.parse(sessionStorage.getItem("clinicaChatHistory")) || [];
  let appointmentState = JSON.parse(sessionStorage.getItem("clinicaAppointmentState")) || {};

  // LÓGICA DE AUTO-APERTURA
  if (sessionStorage.getItem("autoOpenChat") === "true") {
      modal.style.display = "flex";
      sessionStorage.removeItem("autoOpenChat");
  }
  // Cargar historial
  conversationHistory.forEach(chat => addMessageToUI(chat.role, chat.content));

  // Restaurar estado del formulario (Autocompletado al cargar página)
  restaurarEstadoFormulario();

  // ESCUCHA DE EVENTO: CITA REGISTRADA CON ÉXITO
    document.addEventListener('citaRegistrada', () => {
      console.log("🤖 Chatbot: Cita detectada, iniciando despedida...");

      saveAndShowMessage("assistant", "¡Excelente! Tu cita ha sido confirmada con éxito. 🎉<br>Ha sido un placer ayudarte. ¡Cuídate mucho! 👋");
      setTimeout(() => {
          resetConversation(true);
          modal.style.display = "none";
      }, 4000);
      });

  function addMessageToUI(role, text) {
      const msgDiv = document.createElement("div");
      const isBot = role === "assistant";
      msgDiv.className = isBot
          ? "p-2 bg-light rounded mb-2 text-muted small"
          : "p-2 bg-purple text-white rounded ms-auto mb-2 small";
      msgDiv.style.maxWidth = "80%";
      msgDiv.innerHTML = text;
      chatContent.appendChild(msgDiv);
      chatContent.scrollTop = chatContent.scrollHeight;
  }

  function saveAndShowMessage(role, text) {
      addMessageToUI(role, text);
      conversationHistory.push({ role: role, content: text });
      sessionStorage.setItem("clinicaChatHistory", JSON.stringify(conversationHistory));
  }

  function sendMessage() {
    const msg = chatInput.value.trim();
    if (!msg) return;

    saveAndShowMessage("user", msg);
    chatInput.value = "";

    fetch("http://localhost:8085/api/chatbot/chat", {
       method: "POST",
       headers: { "Content-Type": "application/json" },
       body: JSON.stringify({ message: msg, history: conversationHistory })
    })
    .then(response => response.json())
    .then(data => {
      saveAndShowMessage("assistant", data.message);

      // --- LOGICA DE ACCIONES ---

      // 1. Filtrar Especialidad
      if (data.action === "FILTRAR_ESPECIALIDAD" && data.especialidadId) {
          actualizarEstadoYSelect(data.especialidadId, null);
      }

      // 2. Seleccionar Médico (Llega especialidad + medico)
      else if (data.action === "SELECCIONAR_MEDICO" && data.predictedSpecialty) {
          console.log("Bot selecciona médico ID:", data.predictedSpecialty);
          actualizarEstadoYSelect(data.especialidadId, data.predictedSpecialty);
      }

      // 3. Redirigir
      else if (data.action === "REDIRIGIR_CITA") {
                if (!window.location.href.includes("cita.html")) {
                    sessionStorage.setItem("autoOpenChat", "true");

                    setTimeout(() => { window.location.href = "cita.html"; }, 1500);
                }
            }

      // 4. Confirmar Cita
      else if (data.action === "CONFIRMAR_CITA") {
                const btnReservar = document.getElementById("btnReservar");
                if (btnReservar) {
                   const fecha = document.getElementById("fecha")?.value;
                   const hora = document.getElementById("hora")?.value;

                   if (!fecha || !hora) {
                       saveAndShowMessage("assistant", "⚠️ Espera, aún falta seleccionar la fecha y hora en el formulario. Por favor elígelas y avísame.");
                   } else {
                       btnReservar.click();
                   }
                }
            }
    })
    .catch(error => console.error("Error:", error));
  }

  // --- CASCADA DE SELECCIÓN ---
  function actualizarEstadoYSelect(especialidadId, medicoId) {
      if (especialidadId) appointmentState.especialidadId = especialidadId;
      if (medicoId) appointmentState.medicoId = medicoId;
      sessionStorage.setItem("clinicaAppointmentState", JSON.stringify(appointmentState));

      // Si estamos en el formulario, aplicamos los cambios
      const selectEsp = document.getElementById("especialidad");
      if (selectEsp) {
          if (especialidadId) {
              selectEsp.value = especialidadId;
              selectEsp.dispatchEvent(new Event('change')); // Esto carga los médicos

              // Si también hay médico, esperamos a que cargue la lista
              if (medicoId) {
                  intentarSeleccionarMedico(medicoId);
              }
          }
      }
  }

  // Función Paciente: Reintenta buscar al médico en el select
  function intentarSeleccionarMedico(medicoId) {
      let intentos = 0;
      const maxIntentos = 20; // 10 segundos máx

      const checkInterval = setInterval(() => {
          const selectMedico = document.getElementById("medico");
          // Buscamos si la opción ya existe en el HTML
          const opcion = Array.from(selectMedico.options).find(opt => opt.value == medicoId);

          if (opcion) {
              clearInterval(checkInterval);
              selectMedico.value = medicoId;
              selectMedico.dispatchEvent(new Event('change')); // Para cargar horarios
              resaltarElemento(selectMedico);
              console.log("✅ Médico seleccionado automáticamente");
          }

          intentos++;
          if (intentos >= maxIntentos) clearInterval(checkInterval);
      }, 500); // Revisar cada medio segundo
  }

  function resaltarElemento(el) {
      el.scrollIntoView({ behavior: "smooth", block: "center" });
      el.style.border = "2px solid #7b2cbf";
      setTimeout(() => el.style.border = "1px solid #ccc", 2000);
  }

  function restaurarEstadoFormulario() {
      if (document.getElementById("especialidad") && appointmentState.especialidadId) {
          // Esperamos a que la página cargue sus especialidades iniciales
          setTimeout(() => {
              actualizarEstadoYSelect(appointmentState.especialidadId, appointmentState.medicoId);
          }, 1000);
      }
  }

  // --- EVENTOS Y RESET ---
  function resetConversation() {
      sessionStorage.removeItem("clinicaChatHistory");
      sessionStorage.removeItem("clinicaAppointmentState");
      conversationHistory = [];
      appointmentState = {};
      chatContent.innerHTML = "";

      if(document.getElementById("formCita")) document.getElementById("formCita").reset();
            saveAndShowMessage("assistant", "¡Hola! 👋 Soy el asistente virtual de la Clínica San Martín. ¿En qué puedo ayudarte?");
  }

  openBtn.addEventListener("click", () => {
    modal.style.display = "flex";
    chatInput.focus();
    if (conversationHistory.length === 0) {
        saveAndShowMessage("assistant", "¡Hola! 👋 Soy el asistente virtual de la Clínica San Martín. ¿En qué puedo ayudarte?");
    }
  });

  closeBtn.addEventListener("click", () => modal.style.display = "none");
  if(resetBtn) resetBtn.addEventListener("click", () => {
      if(confirm("¿Deseas borrar la conversación?")) resetConversation();
  });

  sendBtn.addEventListener("click", sendMessage);
  chatInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter") sendMessage();
  });
});