// Este código maneja el formulario de inicio de sesión de forma asíncrona.

// URL base de tu API de Spring Boot.
// Es una buena práctica guardarla en una constante para facilitar la edición.
const API_BASE = "http://localhost:8080";

// Espera a que el DOM esté completamente cargado antes de añadir el listener.
document.addEventListener('DOMContentLoaded', () => {

    // Obtener la referencia al formulario de login.
    const loginForm = document.getElementById("loginForm");
    // Obtener la referencia al elemento donde se mostrarán los errores.
    const errorBox = document.getElementById("loginError");

    // Verificar si los elementos existen para evitar errores en el script.
    if (!loginForm || !errorBox) {
        console.error("No se encontraron los elementos 'loginForm' o 'loginError'. Asegúrate de que los IDs sean correctos en tu HTML.");
        return;
    }

    // Agregar un 'event listener' para el evento 'submit' del formulario.
    loginForm.addEventListener("submit", async (e) => {
        // Evitar el comportamiento por defecto del formulario (recargar la página).
        e.preventDefault();

        // Obtener los valores de los campos de email y password.
        const email = document.getElementById("email")?.value?.trim();
        const password = document.getElementById("password")?.value?.trim();

        // Limpiar el mensaje de error anterior.
        errorBox.textContent = "";
        errorBox.style.display = "none";

        // Validar los campos de entrada.
        if (!email || !password) {
            errorBox.textContent = "Por favor, completa todos los campos.";
            errorBox.style.display = "block";
            return;
        }

        try {
            // Realizar una petición a la API de login.
            const response = await fetch(`${API_BASE}/api/auth/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email, password })
            });

            // Si la respuesta no es exitosa (código de estado >= 400), lanzar un error.
            if (!response.ok) {
                let errorMessage = "Error al iniciar sesión. Por favor, verifica tus credenciales.";
                // Intentar leer el mensaje de error del servidor si la respuesta es JSON.
                const contentType = response.headers.get("content-type");
                if (contentType && contentType.includes("application/json")) {
                    const errorData = await response.json();
                    errorMessage = errorData.message || errorMessage;
                }
                throw new Error(errorMessage);
            }

            // Parsear la respuesta JSON y extraer el token.
            const data = await response.json();
            const token = data.token || data.jwt || data.accessToken;
            if (!token) {
                throw new Error("Token no recibido en la respuesta del servidor.");
            }
            
            // Guardar el token de autenticación en el almacenamiento local.
            localStorage.setItem("token", token);
            console.log("Login exitoso. Token almacenado.");

            // Redirigir al usuario a la página principal.
            window.location.href = "index.html";

        } catch (error) {
            // Manejar y mostrar el error de forma amigable.
            console.error("Error durante el login:", error);
            errorBox.textContent = error.message;
            errorBox.style.display = "block";
            // ¡Importante! Evitar 'alert()' que bloquea la interfaz de usuario.
            // La alerta ya no es necesaria con el mensaje en el DOM.
        }
    });
});