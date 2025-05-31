export async function cargarVista() {
    const res = await fetch('./vistas/clientes.html');
    const html = await res.text();
    document.getElementById("contenido").innerHTML = html;

}
    