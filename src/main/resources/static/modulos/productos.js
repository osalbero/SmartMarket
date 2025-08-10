export async function cargarVistaProductos() {
    const res = await fetch('./vistas/productos.html');
    const html = await res.text();
    const contenedor = document.getElementById("contenido");

    // Animación de entrada
    contenedor.classList.remove("animacion-entrada");
    void contenedor.offsetHeight; // Forzar reflujo
    contenedor.innerHTML = html;
    contenedor.classList.add("animacion-entrada");

    // Inicialización de eventos y datos
    await cargarProductos();
    document.getElementById("btnAgregar").addEventListener("click", () => abrirModal());
    document.getElementById("formProducto").addEventListener("submit", guardarProducto);
    document.getElementById("cerrarModal").addEventListener("click", cerrarModal);

    // Evento del formulario de editar precio
    document.getElementById("formEditarPrecio").addEventListener("submit", actualizarPrecioProducto);
    document.getElementById("cerrarModalPrecio").addEventListener("click", cerrarModalPrecio);
    document.getElementById("cerrarModalPrecioX").addEventListener("click", cerrarModalPrecio);

    // Manejo de búsqueda
    document.getElementById("busqueda").addEventListener("input", async function () {
        const texto = this.value.trim();

        if (texto === "") {
            await cargarProductos(); // Vuelve a cargar todos
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/productos/buscar?query=${encodeURIComponent(texto)}`);
            if (!response.ok) throw new Error("Error al buscar productos");
            const resultados = await response.json();
            renderizarTablaProductos(resultados);
        } catch (error) {
            console.error("Error al buscar productos:", error);
            Swal.fire("Error", "No se pudo buscar", "error");
        }
    });
}

async function cargarProductos() {
    try {
        const response = await fetch("http://localhost:8080/api/productos");
        if (!response.ok) throw new Error("Error al obtener productos");
        const productos = await response.json();
        if (!Array.isArray(productos)) throw new Error("Datos de productos no válidos");
        renderizarTablaProductos(productos);
    } catch (error) {
        console.error("Error al cargar productos:", error);
        Swal.fire("Error", "No se pudieron cargar los productos", "error");
    }
}

function renderizarTablaProductos(productos) {
    const body = document.getElementById("tablaProductosBody");
    const termino = document.getElementById("busqueda").value.trim().toLowerCase();
    body.innerHTML = "";

    const resaltar = (texto) => {
        if (!termino) return texto;
        const regex = new RegExp(`(${termino})`, "gi");
        return texto.replace(regex, '<span class="resaltado">$1</span>');
    };

    productos.forEach(p => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
            <td>${resaltar(p.sku)}</td>
            <td>${resaltar(p.nombre)}</td>
            <td>${resaltar(p.descripcion)}</td>
            <td>${resaltar(p.codigoDeBarras)}</td>
            <td>${resaltar(p.categoria.nombre)}</td>
            <td>${p.precioVenta ? `$${p.precioVenta.toFixed(2)}` : "N/A"}</td>
            <td>
                <button class="btn-editar" data-producto='${encodeURIComponent(JSON.stringify(p))}'>Editar</button>                
                <button class="btn-eliminar" data-id='${p.id}' data-nombre='${p.nombre}'>Eliminar</button>
                <button class="btn-editar-precio" data-sku='${p.sku}'>Editar precio</button>
            </td>

        `;
        body.appendChild(fila);
    });

    document.querySelectorAll(".btn-editar").forEach(btn => {
        btn.addEventListener("click", () => {
            const producto = JSON.parse(decodeURIComponent(btn.getAttribute("data-producto")));
            abrirModal(producto);
        });
    });

    document.querySelectorAll(".btn-eliminar").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-id");
            const nombre = btn.getAttribute("data-nombre");
            eliminarProducto(id, nombre);
        });
    });

    document.querySelectorAll(".btn-editar-precio").forEach(btn => {
        btn.addEventListener("click", () => {
            const sku = btn.getAttribute("data-sku");
            abrirModalEditarPrecio(sku);
        });
    });
}

async function abrirModal(producto = null) {
    document.getElementById("tituloModal").innerText = producto ? "Editar Producto" : "Agregar Producto";
    const form = document.getElementById("formProducto");
    form.reset();

    const skuInput = document.getElementById("sku");

    if (producto) {
        skuInput.readOnly = true; // ✅ Solo si estás editando
        skuInput.value = producto.sku;
        document.getElementById("nombre").value = producto.nombre;
        document.getElementById("descripcion").value = producto.descripcion;
        document.getElementById("codigoDeBarras").value = producto.codigoDeBarras;
        document.getElementById("precioVenta").value = producto.precioVenta || 0;
        await llenarSelectCategorias(producto?.categoria?.id);
    } else {
        skuInput.readOnly = false; // ✅ Permitir edición al crear nuevo
        skuInput.value = await generarSku();
        await llenarSelectCategorias();
    }

    document.getElementById("modalProducto").classList.remove("hidden");
}

function cerrarModal() {
    document.getElementById("modalProducto").classList.add("hidden");
}

async function generarSku() {
    const fecha = new Date();
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, "0");
    const prefijo = `PRD${anio}${mes}`;

    try {
        const res = await fetch("http://localhost:8080/api/productos");
        const productos = await res.json();

        const existentes = productos
            .map(p => p.sku)
            .filter(sku => sku.startsWith(prefijo));

        let ultimo = 0;
        existentes.forEach(sku => {
            const num = parseInt(sku.substring(prefijo.length));
            if (!isNaN(num) && num > ultimo) ultimo = num;
        });

        return `${prefijo}${String(ultimo + 1).padStart(3, "0")}`;
    } catch (error) {
        return `${prefijo}001`;
    }
}

async function guardarProducto(e) {
    e.preventDefault();

    // Obtener valores del formulario
    const sku = document.getElementById("sku").value.trim();
    const nombre = document.getElementById("nombre").value.trim();
    const descripcion = document.getElementById("descripcion").value.trim();
    const codigoDeBarras = document.getElementById("codigoDeBarras").value.trim();
    const categoria = document.getElementById("categoria").value;
    const precioVenta = parseFloat(document.getElementById("precioVenta").value);

    // Validación mínima
    if (!nombre || categoria === "__nueva__" || isNaN(parseInt(categoria))) {
        Swal.fire("Error", "Nombre y una categoría válida son obligatorios", "warning");
        return;
    }
    if (isNaN(precioVenta) || precioVenta <= 0) {
        Swal.fire("Error", "Debe ingresar un precio válido mayor a 0", "warning");
        return;
    }


    // Construir objeto producto
    const producto = {
        sku,
        nombre,
        descripcion,
        codigoDeBarras,
        categoria: { id: parseInt(categoria) }, // Asumiendo que el ID de la categoría es un número
        precioVenta,
    };

    // mostrar en consola lo que se va a enviar
    console.log("Guardando producto:", JSON.stringify(producto, null, 2));

    // ✅ Se considera que si el campo SKU está bloqueado (readOnly), es una edición
    const editando = document.getElementById("sku").readOnly;

    // Construcción dinámica de URL y método
    const metodo = editando ? "PUT" : "POST";
    const url = editando
        ? `http://localhost:8080/api/productos/sku/${sku}`
        : "http://localhost:8080/api/productos";

    try {
        const res = await fetch(url, {
            method: metodo,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(producto)
        });

        if (res.ok) {
            const mensaje = editando ? "actualizado" : "creado";
            Swal.fire("Guardado", `Producto ${mensaje} correctamente`, "success");
            cerrarModal();
            await cargarProductos();
        } else {
            const texto = await res.text();
            console.error("Error en respuesta:", texto);
            throw new Error("Error en guardado");
        }
    } catch (error) {
        console.error("Error al guardar producto:", error);
        Swal.fire("Error", "No se pudo guardar el producto", "error");
    }
}

async function eliminarProducto(id, nombre) {
    const confirm = await Swal.fire({
        title: "¿Eliminar producto?",
        text: `¿Estás seguro de eliminar el producto "${nombre}"?`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Sí, eliminar"
    });

    if (confirm.isConfirmed) {
        try {
            const res = await fetch(`http://localhost:8080/api/productos/${id}`, {
                method: "DELETE"
            });

            if (res.ok) {
                Swal.fire("Eliminado", "Producto eliminado correctamente", "success");
                await cargarProductos();
            } else {
                throw new Error("Error al eliminar");
            }
        } catch (error) {
            Swal.fire("Error", "No se pudo eliminar el producto", "error");
        }
    }
}

async function llenarSelectCategorias(seleccionadaId = null) {
    const select = document.getElementById("categoria");
    select.innerHTML = ""; // Limpiar select

    try {
        const response = await fetch("http://localhost:8080/api/categorias");
        if (!response.ok) throw new Error("Error al obtener categorías");

        const categorias = await response.json();

        // Agregar opción por defecto
        const defaultOption = document.createElement("option");
        defaultOption.value = "";
        defaultOption.textContent = "Seleccione una categoría";
        select.appendChild(defaultOption);

        // Agregar categorías existentes
        categorias.forEach(cat => {
            const option = document.createElement("option");
            option.value = cat.id; // 👈 ID correcto
            option.textContent = cat.nombre;
            if (seleccionadaId && parseInt(cat.id) === parseInt(seleccionadaId)) {
                option.selected = true;
            }
            select.appendChild(option);
        });

        // Opción para crear nueva categoría
        const crearNueva = document.createElement("option");
        crearNueva.value = "__nueva__";
        crearNueva.textContent = "+ Crear nueva categoría";
        select.appendChild(crearNueva);

        // Evento para detectar si elige "crear nueva"
        select.onchange = async function () {
            if (this.value === "__nueva__") {
                const { value: nuevaCat } = await Swal.fire({
                    title: 'Nueva Categoría',
                    input: 'text',
                    inputLabel: 'Ingrese el nombre de la nueva categoría',
                    showCancelButton: true,
                    inputValidator: value => value ? null : "El nombre es obligatorio"
                });

                if (nuevaCat) {
                    // Validar si ya existe
                    const yaExiste = Array.from(select.options).some(opt =>
                        opt.textContent.toLowerCase() === nuevaCat.trim().toLowerCase()
                    );

                    if (yaExiste) {
                        Swal.fire("Duplicada", "Esa categoría ya existe", "warning");
                        this.value = "";
                        return;
                    }

                    // Crear nueva categoría en backend
                    const res = await fetch("http://localhost:8080/api/categorias", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ nombre: nuevaCat.trim() })
                    });

                    if (res.ok) {
                        const nueva = await res.json(); // 👈 Recibe categoría con ID
                        const nuevaOpcion = document.createElement("option");
                        nuevaOpcion.value = nueva.id; // 👈 usar ID real
                        nuevaOpcion.textContent = nueva.nombre;
                        nuevaOpcion.selected = true;
                        this.insertBefore(nuevaOpcion, crearNueva);
                        this.value = nueva.id;
                        Swal.fire("Categoría creada", "", "success");
                    } else {
                        Swal.fire("Error", "No se pudo guardar la categoría", "error");
                        this.value = "";
                    }
                } else {
                    this.value = "";
                }
            }
        };

    } catch (error) {
        console.error("Error cargando categorías:", error);
        Swal.fire("Error", "No se pudieron cargar las categorías", "error");
    }
}

function abrirModalEditarPrecio(skuActual) {
    document.getElementById("skuPrecio").value = skuActual;
    document.getElementById("precioNuevo").value = '';
    const modal = document.getElementById("modalEditarPrecio");
    modal.classList.remove("hidden");// quitar la clase hidden
}

function cerrarModalPrecio() {
    document.getElementById("modalEditarPrecio").classList.add("hidden");
}

function actualizarPrecioProducto(event) {
    event.preventDefault();

    const sku = document.getElementById("skuPrecio").value;
    const precioNuevo = document.getElementById("precioNuevo").value;

    fetch(`http://localhost:8080/api/productos/${sku}/precio`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            precio: precioNuevo,
            empleado: {
                id: 1, // id del empleado que realiza el cambio
                nombre: "Óscar Bermúdez" // opcional si tu back solo necesita el id
            }
        })
    })
        .then(res => {
            if (!res.ok) throw new Error("Error en la respuesta del servidor");
            return res.json(); // 👈 Aquí solo funciona si la API devuelve JSON
        })
        .then(data => {
            Swal.fire({
                icon: "success",
                title: data.message
            }).then(() => {
                // Refrescar la tabla o vista
                cargarProductos();
            });
        })
        .catch(error => {
            Swal.fire({
                icon: "error",
                title: "Error",
                text: error.message
            });
        });

        
    document.getElementById("modalEditarPrecio").classList.add("hidden");
}
