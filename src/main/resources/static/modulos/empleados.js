export async function cargarVistaEmpleados() {
    const res = await fetch('./vistas/empleados.html');
    const html = await res.text();
    const contenedor = document.getElementById("contenido");

    // Animación de entrada
    contenedor.classList.remove("animacion-entrada");
    void contenedor.offsetHeight; // Forzar reflujo
    contenedor.innerHTML = html;
    contenedor.classList.add("animacion-entrada");

    // Inicialización de eventos y datos
    await cargarEmpleados();
    document.getElementById("btnAgregar").addEventListener("click", () => abrirModal());
    document.getElementById("formEmpleado").addEventListener("submit", guardarEmpleado);
    document.getElementById("cerrarModal").addEventListener("click", cerrarModal);

    // Manejo de búsqueda
    document.getElementById("busqueda").addEventListener("input", async function () {
        const texto = this.value.trim();

        if (texto === "") {
            await cargarEmpleados(); // Vuelve a cargar todos
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/empleados/buscar?query=${encodeURIComponent(texto)}`);
            if (!response.ok) throw new Error("Error al buscar empleados");
            const resultados = await response.json();
            renderizarTablaEmpleados(resultados);
        } catch (error) {
            console.error("Error al buscar empleados:", error);
            Swal.fire("Error", "No se pudo buscar", "error");
        }
    });
}

async function cargarEmpleados() {
    try {
        const response = await fetch("http://localhost:8080/api/empleados");
        if (!response.ok) throw new Error("Error al obtener empleados");
        const empleados = await response.json();
        // CAMBIO: Se valida si el resultado es un array
        if (!Array.isArray(empleados)) throw new Error("Datos de empleados no válidos");
        renderizarTablaEmpleados(empleados);
    } catch (error) {
        console.error("Error al cargar empleados:", error);
        Swal.fire("Error", "No se pudieron cargar los empleados", "error");
    }
}

function renderizarTablaEmpleados(empleados) {
    const body = document.getElementById("tablaEmpleadosBody");
    const termino = document.getElementById("busqueda").value.trim().toLowerCase();
    body.innerHTML = "";

    const resaltar = (texto) => {
        if (!termino) return texto;
        const regex = new RegExp(`(${termino})`, "gi");
        return texto.replace(regex, '<span class="resaltado">$1</span>');
    };

    empleados.forEach(e => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
                    <td>${resaltar(e.nombre)}</td>
                    <td>${resaltar(e.cargo.nombre)}</td>
                    <td>${resaltar(e.telefono)}</td>
                    <td>${resaltar(e.email)}</td>
                    <td>
                        <button class="btn-editar" data-empleado='${encodeURIComponent(JSON.stringify(e))}'>Editar</button>
                        <button class="btn-eliminar" data-id='${e.id}' data-nombre='${e.nombre}'>Eliminar</button>
                    </td>
                `;
        body.appendChild(fila);
    });

    document.querySelectorAll(".btn-editar").forEach(btn => {
        btn.addEventListener("click", () => {
            const empleado = JSON.parse(decodeURIComponent(btn.getAttribute("data-empleado")));
            abrirModal(empleado);
        });
    });

    document.querySelectorAll(".btn-eliminar").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-id");
            const nombre = btn.getAttribute("data-nombre");
            eliminarEmpleado(id, nombre);
        });
    });
}

async function abrirModal(empleado = null) {
    document.getElementById("tituloModal").innerText = empleado ? "Editar Empleado" : "Agregar Empleado";
    const form = document.getElementById("formEmpleado");
    form.reset();

    if (empleado) {
        document.getElementById("id").value = empleado.id;
        document.getElementById("nombre").value = empleado.nombre;
        document.getElementById("telefono").value = empleado.telefono;
        document.getElementById("email").value = empleado.email;
        grupoId.style.display = "block"; // Mostrar el campo de ID solo si se está editando
        await llenarSelectCargos(empleado?.cargo?.id);
    } else {
        document.getElementById("id").value = "";
        grupoId.style.display = "none"; // Ocultar el campo de ID al agregar un nuevo empleado
        await llenarSelectCargos();
    }
    document.getElementById("modalEmpleado").classList.remove("hidden");
}

function cerrarModal() {
    document.getElementById("modalEmpleado").classList.add("hidden");
}

async function guardarEmpleado(e) {
    e.preventDefault();

    const id = document.getElementById("id").value;
    const nombre = document.getElementById("nombre").value.trim();
    const telefono = document.getElementById("telefono").value.trim();
    const email = document.getElementById("email").value.trim();
    const cargo = document.getElementById("cargo").value;

    if (!nombre || cargo === "__nueva__" || isNaN(parseInt(cargo))) {
        Swal.fire("Error", "Nombre y un cargo válido son obligatorios", "error");
        return;
    }

    const empleado = {
        id,
        nombre,
        cargo: { id: parseInt(cargo) },
        telefono,
        email
    };

    console.log('Objeto a enviar:', JSON.stringify(empleado, null, 2));

    const metodo = id ? "PUT" : "POST";
    const url = id ? `http://localhost:8080/api/empleados/${id}` : "http://localhost:8080/api/empleados";

    try {
        const res = await fetch(url, {
            method: metodo,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(empleado)
        });

        if (res.ok) {
            const mensaje = id ? "actualizado" : "creado";
            Swal.fire("Guardado", `Empleado ${mensaje} correctamente`, "success");

            document.getElementById("formEmpleado").reset();
            document.getElementById("id").value = "";
            cerrarModal();
            await cargarEmpleados();
        } else if (res.status === 409) {
            const data = await res.json().catch(() => ({ message: "El email ya existe" }));
            Swal.fire("Email duplicado", data.message, "warning");
        } else {
            const errorText = await res.text();
            console.error("Error inesperado:", errorText);
            throw new Error("Error inesperado al guardar el empleado");
        }
    } catch (error) {
        console.error("Error:", error);
        Swal.fire("Error", "No se pudo guardar el empleado", "error");
    }
}


async function eliminarEmpleado(id, nombre) {
    const confirm = await Swal.fire({
        title: "¿Estás seguro?",
        text: `¿Deseas eliminar al empleado ${nombre}?`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Sí, eliminar",
        cancelButtonText: "Cancelar"
    });

    if (confirm.isConfirmed) {
        try {
            const res = await fetch(`http://localhost:8080/api/empleados/${id}`, {
                method: "DELETE"
            });

            if (res.ok) {
                Swal.fire("Eliminado", `Empleado ${nombre} eliminado correctamente`, "success");
                await cargarEmpleados();
            } else {
                throw new Error("Error al eliminar el empleado");
            }
        } catch (error) {
            console.error("Error:", error);
            Swal.fire("Error", "No se pudo eliminar el empleado", "error");
        }
    }
}

async function llenarSelectCargos(seleccionadaId = null) {
    const select = document.getElementById("cargo");
    select.innerHTML = ""; // Limpiar select

    try {
        const response = await fetch("http://localhost:8080/api/cargos");
        if (!response.ok) throw new Error("Error al obtener cargos");

        const cargos = await response.json();

        // Agregar opción por defecto
        const defaultOption = document.createElement("option");
        defaultOption.value = "";
        defaultOption.textContent = "Seleccione un cargo";
        select.appendChild(defaultOption);

        // Agregar cargos existentes
        cargos.forEach(cargo => {
            const option = document.createElement("option");
            option.value = cargo.id; // 👈 ID correcto
            option.textContent = cargo.nombre;
            if (seleccionadaId && parseInt(cargo.id) === parseInt(seleccionadaId)) {
                option.selected = true;
            }
            select.appendChild(option);
        });

        // Opción para crear nuevo cargo
        const crearNuevoCargo = document.createElement("option");
        crearNuevoCargo.value = "__nueva__";
        crearNuevoCargo.textContent = "+ Crear nuevo cargo";
        select.appendChild(crearNuevoCargo);

        // Evento para detectar si elige "crear nuevo cargo"
        select.onchange = async function () {
            if (this.value === "__nuevo__") {
                const { value: nuevoCargo } = await Swal.fire({
                    title: 'Nuevo Cargo',
                    input: 'text',
                    inputLabel: 'Ingrese el nombre del nuevo cargo',
                    showCancelButton: true,
                    inputValidator: value => value ? null : "El nombre es obligatorio"
                });

                if (nuevoCargo) {
                    // Validar si ya existe
                    const yaExiste = Array.from(select.options).some(opt =>
                        opt.textContent.toLowerCase() === nuevoCargo.trim().toLowerCase()
                    );

                    if (yaExiste) {
                        Swal.fire("Duplicado", "Ese cargo ya existe", "warning");
                        this.value = "";
                        return;
                    }

                    // Crear nuevo cargo en backend
                    const res = await fetch("http://localhost:8080/api/cargos", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ nombre: nuevoCargo.trim() })
                    });

                    if (res.ok) {
                        const nuevo = await res.json(); // 👈 Recibe cargo con ID
                        const nuevaOpcion = document.createElement("option");
                        nuevaOpcion.value = nuevo.id; // 👈 usar ID real
                        nuevaOpcion.textContent = nuevo.nombre;
                        nuevaOpcion.selected = true;
                        this.insertBefore(nuevaOpcion, crearNuevoCargo);
                        this.value = nuevo.id;
                        Swal.fire("Cargo creado", "", "success");
                    } else {
                        Swal.fire("Error", "No se pudo guardar el cargo", "error");
                        this.value = "";
                    }
                } else {
                    this.value = "";
                }
            }
        };

    } catch (error) {
        console.error("Error cargando cargos:", error);
        Swal.fire("Error", "No se pudieron cargar los cargos", "error");
    }
}