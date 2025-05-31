export async function cargarVista() {
    const res = await fetch('./vistas/categorias.html');
    const html = await res.text();
    document.getElementById("contenido").innerHTML = html;

    await cargarCategorias();
    document.getElementById("btnAgregar").addEventListener("click", () => abrirModal());
    document.getElementById("formCategoria").addEventListener("submit", guardarCategoria);
    document.getElementById("cerrarModal").addEventListener("click", cerrarModal);
}

async function cargarCategorias() {
    try {
        const response = await fetch("http://localhost:8080/api/categorias");
        if (!response.ok) throw new Error("Error al obtener categorias");
        const categorias = await response.json();
        if (!Array.isArray(categorias)) throw new Error("Datos de categorias no válidos");
        renderizarTablaCategorias(categorias);
    } catch (error) {
        console.error("Error al cargar categorias:", error);
        Swal.fire("Error", "No se pudieron cargar las categorias", "error");
    }
}

function renderizarTablaCategorias(categorias) {
    const body = document.getElementById("tablaCategoriasBody");
    body.innerHTML = "";

    categorias.forEach(c => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
            <td>${c.id}</td>
            <td>${c.nombre}</td>            
            <td>
                <button class="btn-editar" data-categoria='${encodeURIComponent(JSON.stringify(c))}'>Editar</button>
                <button class="btn-eliminar" data-id='${c.id}' data-nombre='${c.nombre}'>Eliminar</button>
            </td>
        `;
        body.appendChild(fila);
    });

    document.querySelectorAll(".btn-editar").forEach(btn => {
        btn.addEventListener("click", () => {
            const categoria = JSON.parse(decodeURIComponent(btn.getAttribute("data-categoria")));
            abrirModal(categoria);
        });
    });

    document.querySelectorAll(".btn-eliminar").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-id");
            const nombre = btn.getAttribute("data-nombre");
            eliminarCategoria(id, nombre);
        });
    });
}

async function abrirModal(categoria = null) {
    document.getElementById("tituloModal").innerText = categoria ? "Editar Categoria" : "Agregar Categoria";
    const form = document.getElementById("formCategoria");
    form.reset();

    if (categoria) {
        document.getElementById("id").value = categoria.id;
        document.getElementById("nombre").value = categoria.nombre;
        grupoId.style.display = "block"; // Mostrar el campo de ID solo si se está editando
    } else {
        document.getElementById("id").value = "";
        grupoId.style.display = "none"; // Ocultar el campo de ID al agregar una nueva categoría
    }
    document.getElementById("modalCategoria").classList.remove("hidden");
}

function cerrarModal() {
    document.getElementById("modalCategoria").classList.add("hidden");
}

// Función para capitalizar el nombre de la categoría
function capitalizarNombre(nombre) {
    return nombre
        .toLowerCase()
        .split(" ")
        .filter(p => p !== "")
        .map(p => p.charAt(0).toUpperCase() + p.slice(1))
        .join(" ");
}

async function guardarCategoria(e) {
    e.preventDefault();

    // Obtener valores del formulario
    const id = document.getElementById("id").value;
    const nombreOriginal = document.getElementById("nombre").value.trim();

    // Validar que el nombre no esté vacío
    if (!nombreOriginal) {
        Swal.fire("Error", "El nombre de la categoría es obligatorio", "warning");
        return;
    }

    // Formatear el nombre con la primera letra en mayúscula
    const nombreFormateado = capitalizarNombre(nombreOriginal);

    // Crear objeto categoría
    const categoria = { id, nombre: nombreFormateado };

    // Determinar método y URL según si es creación o actualización
    const metodo = id ? "PUT" : "POST";
    const url = id
        ? `http://localhost:8080/api/categorias/${id}`
        : "http://localhost:8080/api/categorias";

    try {
        const res = await fetch(url, {
            method: metodo,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(categoria)
        });

        // Intentar parsear el cuerpo de la respuesta como JSON
        const data = await res.json();

        if (res.ok) {
            const mensaje = id ? "actualizada" : "creada";
            Swal.fire("Guardado", `Categoría ${mensaje} correctamente`, "success");

            // Limpiar el formulario
            document.getElementById("formCategoria").reset();
            document.getElementById("id").value = "";

            // Cerrar modal y recargar lista
            cerrarModal();
            await cargarCategorias();
        } else if (res.status === 409) {
            // Mostrar mensaje personalizado si es conflicto por duplicado
            Swal.fire("Categoría duplicada", data.message, "warning");
        } else {
            // Otros errores
            throw new Error("Error inesperado al guardar la categoría");
        }
    } catch (error) {
        console.error("Error:", error);
        Swal.fire("Error", "No se pudo guardar la categoría", "error");
    }
}


async function eliminarCategoria(id, nombre) {
    const confirm = await Swal.fire({
        title: "¿Eliminar categoría?",
        text: `¿Estás seguro de eliminar la categoría "${nombre}"?`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Sí, eliminar"
    });

    if (confirm.isConfirmed) {
        try {
            const res = await fetch(`http://localhost:8080/api/categorias/${id}`, {
                method: "DELETE"
            });

            if (!res.ok) throw new Error("Error al eliminar la categoría");

            Swal.fire("Eliminado", "Categoría eliminada correctamente", "success");
            await cargarCategorias();
        } catch (error) {
            Swal.fire("Error", "No se pudo eliminar la categoría", "error");
        }
    }
}