document.addEventListener("DOMContentLoaded", function () {
    const loginForm = document.getElementById('loginForm');
    const errorMessageDiv = document.getElementById('errorMessage');
    
    if (!loginForm || !errorMessageDiv) {
        console.error("Error: No se encontraron los elementos 'loginForm' o 'errorMessage'.");
        return;
    }

    loginForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        errorMessageDiv.classList.add('hidden');
        errorMessageDiv.textContent = '';

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value.trim();

        const loginUrl = 'http://localhost:8080/api/auth/login';

        try {
            const response = await fetch(loginUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) {
                let errorDetails = 'Error en el servidor. Inténtalo de nuevo.';
                const contentType = response.headers.get("content-type");
                
                if (contentType && contentType.includes("application/json")) {
                    const errorData = await response.json();
                    errorDetails = errorData.message || 'Credenciales incorrectas.';
                } else {
                    const textResponse = await response.text();
                    console.error("Respuesta inesperada del servidor:", textResponse);
                    errorDetails = 'Error en el servidor o problema de conexión.';
                }
                throw new Error(errorDetails);
            }

            const data = await response.json();
            localStorage.setItem('token', data.token);

            // Redirigir a la página principal
            window.location.href = './index.html';

        } catch (error) {
            console.error('Error durante el login:', error);
            errorMessageDiv.textContent = `Error: ${error.message}`;
            errorMessageDiv.classList.remove('hidden');
        }
    });
});
