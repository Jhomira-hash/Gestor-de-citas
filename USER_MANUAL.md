# Manual de Usuario - Gestor de Citas

Este manual guía a los usuarios a través de las funcionalidades principales del gestor, proporcionando credenciales de prueba y flujos de trabajo comunes.

## Acceso al Sistema

Para probar el sistema, asegúrese de que tanto el backend como el frontend estén en ejecución (ver Manual Técnico). Abra su navegador en \`http://localhost:8085\`.

## Flujos de Trabajo Principales

### 1. Registro e Inicio de Sesión
Registro: Si es su primera vez, haga clic en "Registrarse" e ingrese sus datos básicos.

Login: Ingrese con sus credenciales. Una vez dentro, el Chatbot de IA se activará automáticamente para darle la bienvenida.

### 2. Consulta y Triaje con IA (Valor Agregado)
Este es el corazón del sistema para evitar errores de especialidad:
- Consulta de Síntomas: Escriba al chatbot cómo se siente (ej: "Me duele mucho la boca del estómago y tengo acidez").
- Recomendación: La IA analizará su mensaje y le sugerirá una especialidad (ej: Gastroenterología) y una lista de doctores disponibles.
- Confirmación: El chatbot le preguntará: "¿Desea agendar una cita con el Dr. [Nombre] en la especialidad de [Especialidad]?".
- Validación de Datos: Confirme sus datos personales cuando el chatbot se lo solicite.

### 3. Agendamiento de Cita
- Redirección Automática: Tras confirmar con la IA, será redirigido al formulario de reserva.
- Autocompletado: El formulario aparecerá con la especialidad y el doctor ya seleccionados por la IA.
- Finalización: Elija la fecha/hora exacta y haga clic en "Confirmar Cita".

### 4. Gestión de Citas (Panel de Usuario)
Si necesita realizar cambios tras el agendamiento:

- Perfil/Dashboard: Ingrese a su sección de perfil.
- Historial y Agenda: Verá un listado de sus Citas Previas y Citas Agendadas.
- Acciones: * Editar: Permite cambiar la fecha o el doctor de una cita próxima.
- Eliminar: Cancela la cita definitivamente, liberando el cupo en la base de datos MySQL.

