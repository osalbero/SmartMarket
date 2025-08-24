import { fetchWithAuth } from "../main.js";
export async function cargarVistaInventarios() {
    const response = await fetch("/vistas/inventarios.html");
    const html = await response.text();
    const contenedor = document.getElementById("contenido");

    // Animación de entrada
    contenedor.classList.remove("animacion-entrada");
    void contenedor.offsetHeight; // Forzar reflujo
    contenedor.innerHTML = html;
    contenedor.classList.add("animacion-entrada");

    setTimeout(() => {
        cargarInventarios();
    }, 0);

    document.getElementById("busqueda").addEventListener("input", async function () {
        const texto = this.value.trim();
        if (texto === "") {
            await cargarInventarios(); // Vuelve a cargar el inventario completo
            return;
        }
        try {
            const response = await fetchWithAuth(`/api/inventario/productos/buscar?query=${encodeURIComponent(texto)}`);
            if (!response.ok) throw new Error("Error al buscar productos");
            const resultados = await response.json();
            cargarInventarios(resultados);
        } catch (error) {
            console.error("Error al buscar productos:", error);
            Swal.fire("Error", "No se pudo buscar", "error");
        }
    });
}

async function cargarInventarios(data = null) {
    const inventario = data || await fetchWithAuth(`/api/inventario/productos`, {
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("token")
        }
    }).then(res => res.json());

    const tbody = document.getElementById("tablaInventarioBody");
    if (!tbody) {
        console.warn("Elemento tablaInventarioBody no encontrado");
        return;
    }

    const busquedaInput = document.getElementById("busqueda");
    const termino = busquedaInput ? busquedaInput.value.trim().toLowerCase() : "";

    tbody.innerHTML = "";

    const resaltar = (texto) => {
        if (!termino) return texto;
        const regex = new RegExp(`(${termino})`, "gi");
        return texto.toString().replace(regex, '<span class="resaltado">$1</span>');
    };

    inventario.forEach(item => {
        const fila = document.createElement("tr");

        if (item.estadoStockActual === "AGOTADO") {
            fila.style.backgroundColor = "#ffe0e0";
            fila.style.color = "#b00000";
            fila.style.fontWeight = "bold";
        }

        fila.innerHTML = `
            <td>${resaltar(item.sku)}</td>
            <td>${resaltar(item.nombre)}</td>
            <td>${resaltar(item.stockDisponible)}</td>
            <td>${resaltar(item.stockBloqueado)}</td>
            <td>${resaltar(item.stockAgotado)}</td>
            <td>${resaltar(item.estadoStockActual)}</td>
            <td>${new Date(item.ultimaActualizacion).toLocaleString()}</td>
        `;
        tbody.appendChild(fila);
    });
}

