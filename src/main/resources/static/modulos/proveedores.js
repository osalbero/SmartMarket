export async function cargarVistaProveedores() {
    const res = await fetch('./vistas/proveedores.html');
    const html = await res.text();
    const contenedor = document.getElementById("contenido");

    // Animación de entrada
    contenedor.classList.remove("animacion-entrada");
    void contenedor.offsetHeight; // Forzar reflujo
    contenedor.innerHTML = html;
    contenedor.classList.add("animacion-entrada");

    // Inicialización de eventos y datos
    await cargarProveedores();
    document.getElementById("btnAgregar").addEventListener("click", () => abrirModal());
    document.getElementById("formProveedor").addEventListener("submit", guardarProveedor);
    document.getElementById("cerrarModal").addEventListener("click", cerrarModal);

    // Manejo de búsqueda
    document.getElementById("busqueda").addEventListener("input", async function () {
        const texto = this.value.trim();

        if (texto === "") {
            await cargarProveedores(); // Vuelve a cargar todos
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/proveedores/buscar?query=${encodeURIComponent(texto)}`);
            if (!response.ok) throw new Error("Error al buscar proveedores");
            const resultados = await response.json();
            renderizarTablaProveedores(resultados);
        } catch (error) {
            console.error("Error al buscar proveedores:", error);
            Swal.fire("Error", "No se pudo buscar", "error");
        }
    });

}

async function cargarProveedores() {
    try {
        const response = await fetch("http://localhost:8080/api/proveedores");
        if (!response.ok) throw new Error("Error al obtener proveedores");
        const proveedores = await response.json();
        if (!Array.isArray(proveedores)) throw new Error("Datos de proveedores no válidos");
        renderizarTablaProveedores(proveedores);
    } catch (error) {
        console.error("Error al cargar proveedores:", error);
        Swal.fire("Error", "No se pudieron cargar los proveedores", "error");
    }
}

function renderizarTablaProveedores(proveedores) {
    const body = document.getElementById("tablaProveedoresBody");
    const termino = document.getElementById("busqueda").value.trim().toLowerCase();
    body.innerHTML = "";

    const resaltar = (texto) => {
        if (!termino) return texto;
        const regex = new RegExp(`(${termino})`, "gi");
        return texto.replace(regex, '<span class="resaltado">$1</span>');
    };

    proveedores.forEach(p => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
            <td>${p.id}</td>
            <td>${resaltar(p.nombre)}</td>
            <td>${resaltar(p.direccion)}</td>
            <td>${resaltar(p.telefono)}</td>
            <td>${resaltar(p.email)}</td>
            <td>
                <button class="btn-editar" data-proveedor='${encodeURIComponent(JSON.stringify(p))}'>Editar</button>
                <button class="btn-eliminar" data-id='${p.id}' data-nombre='${p.nombre}'>Eliminar</button>
            </td>
        `;
        body.appendChild(fila);
    });

    document.querySelectorAll(".btn-editar").forEach(btn => {
        btn.addEventListener("click", () => {
            const proveedor = JSON.parse(decodeURIComponent(btn.getAttribute("data-proveedor")));
            abrirModal(proveedor);
        });
    });

    document.querySelectorAll(".btn-eliminar").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-id");
            const nombre = btn.getAttribute("data-nombre");
            eliminarProveedor(id, nombre);
        });
    });
}



async function abrirModal(proveedor = null) {
    document.getElementById("tituloModal").innerText = proveedor ? "Editar Proveedor" : "Agregar Proveedor";
    const form = document.getElementById("formProveedor");
    form.reset();

    if (proveedor) {
        document.getElementById("id").value = proveedor.id;
        document.getElementById("nombre").value = proveedor.nombre;
        document.getElementById("direccion").value = proveedor.direccion;
        document.getElementById("telefono").value = proveedor.telefono;
        document.getElementById("email").value = proveedor.email;
        grupoId.style.display = "block"; // Mostrar el campo de ID solo si se está editando
    } else {
        document.getElementById("id").value = "";
        grupoId.style.display = "none"; // Ocultar el campo de ID al agregar un nuevo proveedor
    }
    document.getElementById("modalProveedor").classList.remove("hidden");
}

function cerrarModal() {
    document.getElementById("modalProveedor").classList.add("hidden");
}

async function guardarProveedor(e) {
    e.preventDefault();

    const id = document.getElementById("id").value;
    const nombre = document.getElementById("nombre").value.trim();
    const direccion = document.getElementById("direccion").value.trim();
    const telefono = document.getElementById("telefono").value.trim();
    const email = document.getElementById("email").value.trim();

    if (!nombre || !direccion || !telefono || !email) {
        Swal.fire("Error", "Todos los campos son obligatorios", "error");
        return;
    }

    const proveedor = { id, nombre, direccion, telefono, email };
    const metodo = id ? "PUT" : "POST";
    const url = id ? `http://localhost:8080/api/proveedores/${id}` : "http://localhost:8080/api/proveedores";

    try {
        const res = await fetch(url, {
            method: metodo,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(proveedor)
        });

        if (res.ok) {
            // ✅ Proveedor creado o actualizado exitosamente
            const mensaje = id ? "actualizado" : "creado";
            Swal.fire("Guardado", `Proveedor ${mensaje} correctamente`, "success");

            document.getElementById("formProveedor").reset();
            document.getElementById("id").value = "";
            cerrarModal();
            await cargarProveedores();
        } else if (res.status === 409) {
            // ✅ Manejar error de email duplicado
            const data = await res.json().catch(() => ({ message: "El email ya existe" }));
            Swal.fire("Email duplicado", data.message, "warning");
        } else {
            // ❌ Otro tipo de error
            const errorText = await res.text();
            console.error("Error inesperado:", errorText);
            throw new Error("Error inesperado al guardar el proveedor");
        }

    } catch (error) {
        console.error("Error:", error);
        Swal.fire("Error", "No se pudo guardar el proveedor", "error");
    }
}


async function eliminarProveedor(id, nombre) {
    const confirm = await Swal.fire({
        title: "¿Estás seguro?",
        text: `¿Deseas eliminar al proveedor ${nombre}?`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Sí, eliminar",
        cancelButtonText: "Cancelar"
    });

    if (confirm.isConfirmed) {
        try {
            const res = await fetch(`http://localhost:8080/api/proveedores/${id}`, {
                method: "DELETE"
            });

            if (res.ok) {
                Swal.fire("Eliminado", `Proveedor ${nombre} eliminado correctamente`, "success");
                await cargarProveedores();
            } else {
                throw new Error("Error al eliminar el proveedor");
            }
        } catch (error) {
            console.error("Error:", error);
            Swal.fire("Error", "No se pudo eliminar el proveedor", "error");
        }
    }
}