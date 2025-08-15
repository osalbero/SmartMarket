// modulos/main.js
import { cargarVistaProductos } from './modulos/productos.js';
import { cargarVistaCategorias } from './modulos/categorias.js';
import { cargarVistaCargos } from './modulos/cargos.js';
import { cargarVistaClientes } from './modulos/clientes.js';
import { cargarVistaEmpleados } from './modulos/empleados.js';
import { cargarVistaProveedores } from './modulos/proveedores.js';
import { cargarVistaMovimientosInventario } from './modulos/movimientos_inventario.js';
import { cargarVistaVentas } from './modulos/ventas.js';

// Función para mostrar la vista de inicio
function mostrarInicio() {
    const contenido = document.getElementById("contenido");

    //Reiniciar animación
    contenido.classList.remove("animacion-entrada");
    void contenido.offsetHeight; // Forzar reflujo para reiniciar la animación

    contenido.innerHTML = `
        <div class="welcome-card">
            <div class="icon">📈</div>
            <h1>Bienvenido a SmartMarket</h1>
            <p>Gestiona productos, ventas, clientes y mucho más desde un solo lugar.</p>
        </div>
    `;
    contenido.classList.add("animacion-entrada");
}

// Función para cerrar el sidebar (menú hamburguesa)
function cerrarSidebar() {
    const sidebar = document.getElementById("sidebar");
    const content = document.getElementById("contenido");
    const body = document.body;

    sidebar.classList.remove("active");
    content.classList.remove("shifted");
    body.classList.remove("sidebar-open");
}

// Función para manejar el cierre de sesión
function logout() {
    Swal.fire({
        title: '¿Estás seguro?',
        text: '¡Se cerrará tu sesión!',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Sí, cerrar sesión',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            // Eliminar el token de autenticación del localStorage
            localStorage.removeItem('token');
            // Redirigir a la página de login
            window.location.href = './login.html';
        }
    });
}

// Eventos para el menú lateral
document.addEventListener("DOMContentLoaded", () => {
    // --- VERIFICACIÓN DE AUTENTICACIÓN ---
    // Obtener el token de autenticación del localStorage
    const token = localStorage.getItem('token');

    // Si no hay token, el usuario no está autenticado. Redirigirlo a la página de login.
    if (!token) {
        console.log("Token no encontrado. Redirigiendo a la página de login.");
        window.location.href = '/login.html';
        return; // Detener la ejecución del resto del script
    }
    // --- FIN VERIFICACIÓN DE AUTENTICACIÓN ---
    
    const sidebar = document.getElementById("sidebar");
    const hamburgerMenu = document.getElementById("hamburger-menu");
    const content = document.getElementById("contenido");

    // Evento para abrir/cerrar el menú hamburguesa
    hamburgerMenu.addEventListener("click", () => {
        sidebar.classList.toggle("active");
        content.classList.toggle("shifted");
        document.body.classList.toggle("sidebar-open");
    });

    // Evento para cerrar el menú si se hace clic fuera de él (en el overlay del contenido)
    content.addEventListener("click", (e) => {
        if (sidebar.classList.contains("active") && !sidebar.contains(e.target) && e.target !== hamburgerMenu) {
            cerrarSidebar();
        }
    });

    // Evento para el nuevo botón de Cerrar Sesión
    document.getElementById("logout-button").addEventListener("click", e => {
        e.preventDefault();
        logout();
        cerrarSidebar(); // Cerrar el menú después de iniciar la acción de logout
    });

    // Asegurarse de cerrar el sidebar al seleccionar una opción
    document.getElementById("menu-productos").addEventListener("click", e => {
        e.preventDefault();
        cargarVistaProductos();
        cerrarSidebar(); // Cerrar al seleccionar
    });

    document.getElementById("menu-movimientos-inventario").addEventListener("click", e => {
        e.preventDefault();
        cargarVistaMovimientosInventario();
        cerrarSidebar(); // Cerrar al seleccionar
    });

    document.getElementById("menu-ventas").addEventListener("click", e => {
        e.preventDefault();
        cargarVistaVentas();
        cerrarSidebar(); // Cerrar al seleccionar
    });

    document.getElementById("menu-categorias").addEventListener("click", e => {
        e.preventDefault();
        cargarVistaCategorias();
        cerrarSidebar(); // Cerrar al seleccionar
    });

    document.getElementById("menu-cargos").addEventListener("click", e => {
        e.preventDefault();
        cargarVistaCargos();
        cerrarSidebar(); // Cerrar al seleccionar
    });

    document.getElementById("menu-clientes").addEventListener("click", e => {
        e.preventDefault();
        cargarVistaClientes();
        cerrarSidebar(); // Cerrar al seleccionar
    });

    document.getElementById("menu-empleados").addEventListener("click", e => {
        e.preventDefault();
        cargarVistaEmpleados();
        cerrarSidebar(); // Cerrar al seleccionar
    });

    document.getElementById("menu-proveedores").addEventListener("click", e => {
        e.preventDefault();
        cargarVistaProveedores();
        cerrarSidebar(); // Cerrar al seleccionar
    });

    document.getElementById("menu-inicio").addEventListener("click", e => {
        e.preventDefault();
        mostrarInicio();
        cerrarSidebar(); // Cerrar al seleccionar
    });

    // Cargar la vista de inicio al arrancar la aplicación (solo si hay un token)
    mostrarInicio();
});