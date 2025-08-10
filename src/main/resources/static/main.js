// modulos/main.js
import { cargarVistaProductos } from './modulos/productos.js';
import { cargarVistaCategorias } from './modulos/categorias.js';
import { cargarVistaCargos } from './modulos/cargos.js';
import { cargarVistaClientes } from './modulos/clientes.js';
import { cargarVistaEmpleados } from './modulos/empleados.js';
import { cargarVistaProveedores } from './modulos/proveedores.js';
import { cargarVistaMovimientosInventario } from './modulos/movimientos_inventario.js';
import { cargarVistaVentas } from './modulos/ventas.js';

// Mostrar la vista de inicio
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

// Eventos para el menú lateral
document.addEventListener("DOMContentLoaded", () => {
    const sidebar = document.getElementById("sidebar");
    const hamburgerMenu = document.getElementById("hamburger-menu");
    const content = document.getElementById("contenido"); // Obtener el contenido para el overlay

    // Evento para abrir/cerrar el menú hamburguesa
    hamburgerMenu.addEventListener("click", () => {
        sidebar.classList.toggle("active");
        content.classList.toggle("shifted"); // Desplaza el contenido
        document.body.classList.toggle("sidebar-open"); // Deshabilita scroll en body
    });

    // Evento para cerrar el menú si se hace clic fuera de él (en el overlay del contenido)
    content.addEventListener("click", (e) => {
        if (sidebar.classList.contains("active") && !sidebar.contains(e.target) && e.target !== hamburgerMenu) {
            cerrarSidebar();
        }
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

    mostrarInicio(); // Cargar inicio al arrancar
});