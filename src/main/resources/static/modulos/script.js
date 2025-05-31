document.addEventListener("DOMContentLoaded", () => {
    // Botón de Productos
    document.getElementById("menu-productos").addEventListener("click", (e) => {
        e.preventDefault();
        cargarVistaProductos();
    });

    // Botón de Categorías
    document.getElementById("menu-categorias").addEventListener("click", (e) => {
        e.preventDefault();
        cargarVistaCategorias();
    });

    // Botón de Clientes
    document.getElementById("menu-clientes").addEventListener("click", (e) => {
        e.preventDefault();
        cargarVistaClientes();
    });

    

    // Botón "Menú" para volver a la página de inicio
    const btnInicio = document.getElementById("menu-inicio");
    if (btnInicio) {
        btnInicio.addEventListener("click", (e) => {
            e.preventDefault();
            mostrarInicio();
        });
    }
    
    mostrarInicio(); // Mostrar la página de inicio al cargar


});

// Función para mostrar la página de inicio
function mostrarInicio() {
    const contenido = document.getElementById("contenido");
    contenido.innerHTML = `
        <div class="welcome-card">
    <div class="icon">📈</div>
    <h1>Bienvenido a SmartMarket</h1>
    <p>Gestiona productos, ventas, clientes y mucho más desde un solo lugar.</p>
    </div>
    `;
}



// Captura todos los enlaces del menú
const menuItems = document.querySelectorAll('.sidebar ul li a');

menuItems.forEach(item => {
    item.addEventListener('click', function (e) {
        e.preventDefault(); // Evita recargar la página

        // Quita la clase 'active' de todos los enlaces
        menuItems.forEach(i => i.classList.remove('active'));

        // Agrega la clase 'active' al enlace clicado
        this.classList.add('active');

        // Cargar el módulo correspondiente en #contenido (ejemplo básico)
        cargarModulo(this.id);
    });
});

// Función de ejemplo para cargar módulos dinámicamente
function cargarModulo(menuId) {
    const contenido = document.getElementById('contenido');
    switch (menuId) {
        case 'menu-productos':
            contenido.innerHTML = '<h1>Gestión de Productos</h1>';
            break;
        case 'menu-categorias':
            contenido.innerHTML = '<h1>Gestión de Categorías</h1>';
            break;
        case 'menu-proveedores':
            contenido.innerHTML = '<h1>Gestión de Proveedores</h1>';
            break;
        case 'menu-clientes':
            contenido.innerHTML = '<h1>Gestión de Clientes</h1>';
            break;
        case 'menu-ventas':
            contenido.innerHTML = '<h1>Gestión de Ventas</h1>';
            break;
        case 'menu-compras':
            contenido.innerHTML = '<h1>Gestión de Compras</h1>';
            break;
        case 'menu-reportes':
            contenido.innerHTML = '<h1>Reportes</h1>';
            break;
        case 'menu-configuraciones':
            contenido.innerHTML = '<h1>Configuración</h1>';
            break;
        case 'menu-cierre-caja':
            contenido.innerHTML = '<h1>Cierre de Caja</h1>';
            break;
        case 'menu-empleados':
            contenido.innerHTML = '<h1>Gestión de Empleados</h1>';
            break;
        default:
            contenido.innerHTML = '<h1>Módulo no encontrado</h1>';
    }
}
