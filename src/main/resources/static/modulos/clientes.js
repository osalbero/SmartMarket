export async function cargarVistaClientes() {
    const res = await fetch('./vistas/clientes.html');
    const html = await res.text();
    const contenedor = document.getElementById("contenido");

    // Animación de entrada
    contenedor.classList.remove("animacion-entrada");
    void contenedor.offsetHeight; // Forzar reflujo
    contenedor.innerHTML = html;
    contenedor.classList.add("animacion-entrada");

    // Inicialización de eventos y datos
    await cargarClientes();
    document.getElementById("btnAgregar").addEventListener("click", () => abrirModal());
    document.getElementById("formCliente").addEventListener("submit", guardarCliente);
    document.getElementById("cerrarModal").addEventListener("click", cerrarModal);
    
    // Manejo de búsqueda
    document.getElementById("busqueda").addEventListener("input", async function () {
        const texto = this.value.trim();

        if (texto === "") {
            await cargarClientes(); // Vuelve a cargar todos
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/clientes/buscar?query=${encodeURIComponent(texto)}`);
            if (!response.ok) throw new Error("Error al buscar clientes");
            const resultados = await response.json();
            renderizarTablaClientes(resultados);
        } catch (error) {
            console.error("Error al buscar clientes:", error);
            Swal.fire("Error", "No se pudo buscar", "error");
        }
    });
}


async function cargarClientes() {
    try {
        const response = await fetch("http://localhost:8080/api/clientes");
        if (!response.ok) throw new Error("Error al obtener clientes");
        const clientes = await response.json();
        if (!Array.isArray(clientes)) throw new Error("Datos de clientes no válidos");
        renderizarTablaClientes(clientes);
    } catch (error) {
        console.error("Error al cargar clientes:", error);
        Swal.fire("Error", "No se pudieron cargar los clientes", "error");
    }
}

function renderizarTablaClientes(clientes) {
    const body = document.getElementById("tablaClientesBody");
    const termino = document.getElementById("busqueda").value.trim().toLowerCase();
    body.innerHTML = "";

    const resaltar = (texto) => {
        if (!termino) return texto;
        const regex = new RegExp(`(${termino})`, "gi");
        return texto.replace(regex, '<span class="resaltado">$1</span>');
    };

    clientes.forEach(c => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
            <td>${c.id}</td>
            <td>${resaltar(c.nombre)}</td>
            <td>${resaltar(c.email)}</td>
            <td>${resaltar(c.telefono)}</td>
            <td>
                <button class="btn-editar" data-cliente='${encodeURIComponent(JSON.stringify(c))}'>Editar</button>
                <button class="btn-eliminar" data-id='${c.id}' data-nombre='${c.nombre}'>Eliminar</button>
            </td>
        `;
        body.appendChild(fila);
    });

    document.querySelectorAll(".btn-editar").forEach(btn => {
        btn.addEventListener("click", () => {
            const cliente = JSON.parse(decodeURIComponent(btn.getAttribute("data-cliente")));
            abrirModal(cliente);
        });
    });

    document.querySelectorAll(".btn-eliminar").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-id");
            const nombre = btn.getAttribute("data-nombre");
            eliminarCliente(id, nombre);
        });
    });
}

async function abrirModal(cliente = null) {
    document.getElementById("tituloModal").innerText = cliente ? "Editar Cliente" : "Agregar Cliente";
    const form = document.getElementById("formCliente");
    form.reset();

    if (cliente) {
        document.getElementById("id").value = cliente.id;
        document.getElementById("nombre").value = cliente.nombre;
        document.getElementById("email").value = cliente.email;
        document.getElementById("telefono").value = cliente.telefono;
        grupoId.style.display = "block"; // Mostrar el campo de ID solo si se está editando
    } else {
        document.getElementById("id").value = "";
        grupoId.style.display = "none"; // Ocultar el campo de ID al agregar un nuevo cliente
    }
    document.getElementById("modalCliente").classList.remove("hidden");
}

function cerrarModal() {
    document.getElementById("modalCliente").classList.add("hidden");
}

async function guardarCliente(e) {
    e.preventDefault();

    const id = document.getElementById("id").value;
    const nombre = document.getElementById("nombre").value.trim();
    const email = document.getElementById("email").value.trim();
    const telefono = document.getElementById("telefono").value.trim();

    if (!nombre || !email || !telefono) {
        Swal.fire("Error", "Todos los campos son obligatorios", "error");
        return;
    }

    const cliente = { id, nombre, email, telefono };
    const metodo = id ? "PUT" : "POST";
    const url = id ? `http://localhost:8080/api/clientes/${id}` : "http://localhost:8080/api/clientes";

    try {
        const res = await fetch(url, {
            method: metodo,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(cliente)
        });

        if (res.ok) {
            // ✅ Cliente creado o actualizado exitosamente
            const mensaje = id ? "actualizado" : "creado";
            Swal.fire("Guardado", `Cliente ${mensaje} correctamente`, "success");

            document.getElementById("formCliente").reset();
            document.getElementById("id").value = "";
            cerrarModal();
            await cargarClientes();
        } else if (res.status === 409) {
            // ✅ Manejar error de email duplicado
            const data = await res.json().catch(() => ({ message: "El email ya existe" }));
            Swal.fire("Email duplicado", data.message, "warning");
        } else {
            // ❌ Otro tipo de error
            const errorText = await res.text();
            console.error("Error inesperado:", errorText);
            throw new Error("Error inesperado al guardar el cliente");
        }

    } catch (error) {
        console.error("Error:", error);
        Swal.fire("Error", "No se pudo guardar el cliente", "error");
    }
}


async function eliminarCliente(id, nombre) {
    const confirm = await Swal.fire({
        title: "¿Estás seguro?",
        text: `¿Deseas eliminar al cliente ${nombre}?`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Sí, eliminar",
        cancelButtonText: "Cancelar"
    });

    if (confirm.isConfirmed) {
        try {
            const res = await fetch(`http://localhost:8080/api/clientes/${id}`, {
                method: "DELETE"
            });

            if (res.ok) {
                Swal.fire("Eliminado", `Cliente ${nombre} eliminado correctamente`, "success");
                await cargarClientes();
            } else {
                throw new Error("Error al eliminar el cliente");
            }
        } catch (error) {
            console.error("Error:", error);
            Swal.fire("Error", "No se pudo eliminar el cliente", "error");
        }
    }
}