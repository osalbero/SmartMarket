// Este script se encarga de proteger las páginas que requieren autenticación.

document.addEventListener('DOMContentLoaded', () => {
    // Obtener el token de autenticación del almacenamiento local.
    const token = localStorage.getItem('token');

    // Si no hay un token, o el token es nulo o una cadena vacía,
    // redirigir al usuario de vuelta a la página de login.
    // Esto evita que los usuarios no autenticados accedan a la página principal.
    if (!token) {
        console.log("No se encontró un token de autenticación. Redirigiendo al login...");
        window.location.href = "login.html";
    } else {
        // Si el token existe, se asume que el usuario está autenticado.
        // Aquí podrías agregar lógica para validar el token con el servidor
        // si es necesario (ej. para verificar que no ha expirado).
        console.log("Usuario autenticado. Token encontrado.");
    }

    // Aquí puedes añadir más lógica de la página principal, como
    // cargar datos del usuario, mostrar contenido protegido, etc.
});