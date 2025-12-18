# Manual Técnico - Gestor de Citas
Este documento describe la configuración técnica, instalación y puesta en marcha del proyecto **Gestor de citas** . El sistema es una plataforma SaaS compuesta por un backend en Java y un frontend con HTML y CSS.

## Requisitos Previos
 Antes de comenzar, asegúrate de tener instalado
 
- [Git](https://git-scm.com/)
- [MySql](https://www.mysql.com/)
-  [JDK](https://www.oracle.com/java/technologies/downloads/)

 ## Estructura del Proyecto

El repositorio es un monorepo que contiene:

- \`main/resources/\`: Interfaz de usuario 
- \`main/java/com/clinica/\`: API RESTful, modelos de base de datos y lógica de negocio.

- ## Instalación y Configuración

### 1. Backend
  1. Configura las variables de entorno:
     Modifica el archivo .yml modificando las credenciales de la conexión a BDD y el apiKey de groq.
  2. Iniciar el servidor
### 2. Frontend
1.  Ingresa al puerto 8085 \`http://localhost:8085\`.
## API Endpoints Principales

La API expone los siguientes recursos bajo \`/api\`:

- \`/usuarios\`: Gestión de usuarios (auth, perfil).
- \`/medicos\`: Gestión de medicos.
- \`/horarios\`: Gestión de horarios.
- \`/especialidades\`: Gestión de especialidades.
- \`/citas\`: Gestión de citas.
- \`/chatbot\`: Gestión del chatbot.

## Tecnologías

- **Backend:** Java, SpringBoot, MySQl.
- **Frontend:** HTML, CSS, Bootstrap.
