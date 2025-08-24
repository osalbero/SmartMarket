import { fetchWithAuth } from "../main.js";
const API_BASE = "http://localhost:8080";

function getEmailFromToken(token) {
    try {
        const payloadBase64 = token.split('.')[1];
        const decodedPayload = JSON.parse(atob(payloadBase64));
        return decodedPayload.sub || decodedPayload.email || "";
    } catch (error) {
        console.error("Error al decodificar el token:", error);
        return "";
    }
}

const token = localStorage.getItem("token");
const emailInput = document.getElementById("email");
const mensaje = document.getElementById("mensaje");

if (token) {
    const email = getEmailFromToken(token);
    emailInput.value = email;
    emailInput.readOnly = true;
    emailInput.classList.add("readonly");
} else {
    mensaje.textContent = "No se encontró el token. Inicia sesión nuevamente.";
    mensaje.style.color = "red";
}


document.getElementById("formCambioPassword").addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = getEmailFromToken(token);
    const actual = document.getElementById("actual")?.value?.trim();
    const nueva = document.getElementById("nueva")?.value?.trim();
    const mensaje = document.getElementById("mensaje");

    mensaje.textContent = "";
    mensaje.style.color = "red";

    if (!email || !actual || !nueva) {
        mensaje.textContent = "Por favor, completa todos los campos.";
        return;
    }

    if (!token) {
        mensaje.textContent = "No se encontró el token. Inicia sesión nuevamente.";
        return;
    }

    try {
        const response = await fetchWithAuth(`/api/auth/cambiar-password`, {
            method: "POST",
            body: JSON.stringify({
                email,
                contraseñaActual: actual,
                nuevaContraseña: nueva
            })
        });

        if (!response.ok) {
            const contentType = response.headers.get("content-type");
            let errorMessage = "Error al actualizar la contraseña.";
            if (contentType && contentType.includes("application/json")) {
                const errorData = await response.json();
                errorMessage = errorData.message || errorMessage;
            }
            throw new Error(errorMessage);
        }

        mensaje.textContent = "Contraseña actualizada correctamente.";
        mensaje.style.color = "green";

        setTimeout(() => {
            window.location.href = "index.html";
        }, 2000);

    } catch (error) {
        console.error("Error al cambiar la contraseña:", error);
        mensaje.textContent = error.message;
    }
});