document.getElementById("menu-productos").addEventListener("click", async () => {
    const modulo = await import('./modulos/productos.js');
    modulo.cargarVista();
});

document.getElementById("menu-clientes").addEventListener("click", async () => {
    const modulo = await import('./modulos/clientes.js');
    modulo.cargarVista();
});

document.getElementById("menu-ventas").addEventListener("click", async () => {
    const modulo = await import('./modulos/ventas.js');
    modulo.cargarVista();
});

document.getElementById("menu-categorias").addEventListener("click", async () => {
    const modulo = await import('./modulos/categorias.js');
    modulo.cargarVista();
});

document.getElementById("menu-proveedores").addEventListener("click", async () => {
    const modulo = await import('./modulos/proveedores.js');
    modulo.cargarVista();
});

document.getElementById("menu-empleados").addEventListener("click", async () => {
    const modulo = await import('./modulos/empleados.js');
    modulo.cargarVista();
});

document.getElementById("menu-reportes").addEventListener("click", async () => {
    const modulo = await import('./modulos/reportes.js');
    modulo.cargarVista();
});



