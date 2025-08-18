import { fetchWithAuth } from "../main.js";
export async function cargarVistaCargos() {
    const res = await fetch('./vistas/cargos.html');
    const html = await res.text();
    const contenedor = document.getElementById("contenido");

    // Animación de entrada
    contenedor.classList.remove("animacion-entrada");
    void contenedor.offsetHeight; // Forzar reflujo
    contenedor.innerHTML = html;
    contenedor.classList.add("animacion-entrada");

    // Inicialización de eventos y datos
    await cargarCargos();
    document.getElementById("btnAgregar").addEventListener("click", () => abrirModal());
    document.getElementById("formCargo").addEventListener("submit", guardarCargo);
    document.getElementById("cerrarModal").addEventListener("click", cerrarModal);

    // Manejo de búsqueda
    document.getElementById("busqueda").addEventListener("input", async function () {
        const texto = this.value.trim();

        if (texto === "") {
            await cargarCargos(); // Vuelve a cargar todos
            return;
        }

        try {
            const response = await fetchWithAuth(`/api/cargos/buscar?query=${encodeURIComponent(texto)}`);
            if (!response.ok) throw new Error("Error al buscar cargos");
            const resultados = await response.json();
            renderizarTablaCargos(resultados);
        } catch (error) {
            console.error("Error al buscar cargos:", error);
            Swal.fire("Error", "No se pudo buscar", "error");
        }
    });
}

async function cargarCargos() {
    try {
        const response = await fetchWithAuth("/api/cargos");
        if (!response.ok) throw new Error("Error al obtener cargos");
        const cargos = await response.json();
        if (!Array.isArray(cargos)) throw new Error("Datos de cargos no válidos");
        renderizarTablaCargos(cargos);
    } catch (error) {
        console.error("Error al cargar cargos:", error);
        Swal.fire("Error", "No se pudieron cargar los cargos", "error");
    }
}

function renderizarTablaCargos(cargos) {
    const body = document.getElementById("tablaCargosBody");
    const termino = document.getElementById("busqueda").value.trim().toLowerCase();
    // Resaltar coincidencias en la búsqueda
    body.innerHTML = "";

    const resaltar = (texto) => {
        if (!termino) return texto;
        const regex = new RegExp(`(${termino})`, "gi");
        return texto.replace(regex, '<span class="resaltado">$1</span>');
    };
    cargos.forEach(c => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
            <td>${c.id}</td>
            <td>${resaltar(c.nombre)}</td>            
            <td>
                <button class="btn-editar" data-cargo='${encodeURIComponent(JSON.stringify(c))}'>Editar</button>
                <button class="btn-eliminar" data-id='${c.id}' data-nombre='${c.nombre}'>Eliminar</button>
            </td>
        `;
        body.appendChild(fila);
    });

    document.querySelectorAll(".btn-editar").forEach(btn => {
        btn.addEventListener("click", () => {
            const cargo = JSON.parse(decodeURIComponent(btn.getAttribute("data-cargo")));
            abrirModal(cargo);
        });
    });

    document.querySelectorAll(".btn-eliminar").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-id");
            const nombre = btn.getAttribute("data-nombre");
            eliminarCargo(id, nombre);
        });
    });
}

async function abrirModal(cargo = null) {
    document.getElementById("tituloModal").innerText = cargo ? "Editar Cargo" : "Agregar Cargo";
    const form = document.getElementById("formCargo");
    form.reset();

    if (cargo) {
        document.getElementById("id").value = cargo.id;
        document.getElementById("nombre").value = cargo.nombre;
        grupoId.style.display = "block"; // Mostrar el campo de ID solo si se está editando
    } else {
        document.getElementById("id").value = "";
        grupoId.style.display = "none"; // Ocultar el campo de ID al agregar un nuevo cargo
    }
    document.getElementById("modalCargo").classList.remove("hidden");
}

function cerrarModal() {
    document.getElementById("modalCargo").classList.add("hidden");
}

// Función para capitalizar el nombre del cargo
function capitalizarNombre(nombre) {
    return nombre
        .toLowerCase()
        .split(" ")
        .filter(p => p !== "")
        .map(p => p.charAt(0).toUpperCase() + p.slice(1))
        .join(" ");
}

async function guardarCargo(e) {
    e.preventDefault();

    // Obtener valores del formulario
    const id = document.getElementById("id").value;
    const nombreOriginal = document.getElementById("nombre").value.trim();

    // Validar que el nombre no esté vacío
    if (!nombreOriginal) {
        Swal.fire("Error", "El nombre del cargo es obligatorio", "warning");
        return;
    }

    // Formatear el nombre con la primera letra en mayúscula
    const nombreFormateado = capitalizarNombre(nombreOriginal);

    // Crear objeto cargo
    const cargo = { id, nombre: nombreFormateado };

    // Determinar método y URL según si es creación o actualización
    const metodo = id ? "PUT" : "POST";
    const url = id
        ? `/api/cargos/${id}`
        : "/api/cargos";

    try {
        const res = await fetchWithAuth(url, {
            method: metodo,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(cargo)
        });

        // Intentar parsear el cuerpo de la respuesta como JSON
        const data = await res.json();

        if (res.ok) {
            const mensaje = id ? "actualizada" : "creada";
            Swal.fire("Guardado", `Cargo ${mensaje} correctamente`, "success");

            // Limpiar el formulario
            document.getElementById("formCargo").reset();
            document.getElementById("id").value = "";

            // Cerrar modal y recargar lista
            cerrarModal();
            await cargarCargos();
        } else if (res.status === 409) {
            // Mostrar mensaje personalizado si es conflicto por duplicado
            Swal.fire("Cargo duplicado", data.message, "warning");
        } else {
            // Otros errores
            throw new Error("Error inesperado al guardar el cargo");
        }
    } catch (error) {
        console.error("Error:", error);
        Swal.fire("Error", "No se pudo guardar el cargo", "error");
    }
}


async function eliminarCargo(id, nombre) {
    const confirm = await Swal.fire({
        title: "¿Eliminar cargo?",
        text: `¿Estás seguro de eliminar el cargo "${nombre}"?`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Sí, eliminar",
        cancelButtonText: "Cancelar"
    });

    if (confirm.isConfirmed) {
        try {
            const res = await fetchWithAuth(`/api/cargos/${id}`, {
                method: "DELETE"
            });

            if (!res.ok) {
                const errorData = await res.json();
                throw new Error(errorData.message || "Error desconocido al eliminar el cargo");
            }

            Swal.fire("Eliminado", `Cargo "${nombre}" eliminado correctamente`, "success");
            await cargarCargos();
        } catch (error) {
            console.error("Error al eliminar el cargo:", error.message);
            Swal.fire("Error", error.message, "error");
        }
    }
}