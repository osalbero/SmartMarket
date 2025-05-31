export async function cargarVista() {
    const res = await fetch('./vistas/productos.html');
    const html = await res.text();
    document.getElementById("contenido").innerHTML = html;

    await cargarProductos();
    document.getElementById("btnAgregar").addEventListener("click", () => abrirModal());
    document.getElementById("formProducto").addEventListener("submit", guardarProducto);
    document.getElementById("cerrarModal").addEventListener("click", cerrarModal);
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
    body.innerHTML = "";

    productos.forEach(p => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
            <td>${p.sku}</td>
            <td>${p.nombre}</td>
            <td>${p.descripcion}</td>
            <td>${p.codigoDeBarras}</td>
            <td>${p.categoria.nombre}</td>
            <td>
                <button class="btn-editar" data-producto='${encodeURIComponent(JSON.stringify(p))}'>Editar</button>
                <button class="btn-eliminar" data-sku='${p.sku}' data-nombre='${p.nombre}'>Eliminar</button>
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
            const sku = btn.getAttribute("data-sku");
            const nombre = btn.getAttribute("data-nombre");
            eliminarProducto(sku, nombre);
        });
    });
}

async function abrirModal(producto = null) {
    document.getElementById("tituloModal").innerText = producto ? "Editar Producto" : "Agregar Producto";
    const form = document.getElementById("formProducto");
    form.reset();

    const skuInput = document.getElementById("sku");
    skuInput.readOnly = true;

    if (producto) {
        skuInput.value = producto.sku;
        document.getElementById("nombre").value = producto.nombre;
        document.getElementById("descripcion").value = producto.descripcion;
        document.getElementById("codigoDeBarras").value = producto.codigoDeBarras;
        await cargarCategorias(producto.categoria?.nombre);
    } else {
        skuInput.value = await generarSku();
        await cargarCategorias();
    }

    document.getElementById("modalProducto").classList.remove("hidden");
}

function cerrarModal() {
    document.getElementById("modalProducto").classList.add("hidden");
}

async function cargarCategorias(seleccionada = "") {
    const select = document.getElementById("categoria");
    select.innerHTML = "";
    try {
        const response = await fetch("http://localhost:8080/api/categorias");
        const categorias = await response.json();

        categorias.forEach(cat => {
            const option = document.createElement("option");
            option.value = cat.nombre;
            option.textContent = cat.nombre;
            if (cat.nombre === seleccionada) option.selected = true;
            select.appendChild(option);
        });

        const nueva = document.createElement("option");
        nueva.value = "__nueva__";
        nueva.textContent = "+ Crear nueva categoría";
        select.appendChild(nueva);

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
                    const yaExiste = Array.from(select.options).some(opt =>
                        opt.value.toLowerCase() === nuevaCat.toLowerCase()
                    );
                    if (yaExiste) {
                        Swal.fire("Duplicada", "Esa categoría ya existe", "warning");
                        this.value = "";
                        return;
                    }

                    const res = await fetch("http://localhost:8080/api/categorias", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ nombre: nuevaCat.trim() })
                    });

                    if (res.ok) {
                        const nuevaOpcion = document.createElement("option");
                        nuevaOpcion.value = nuevaCat.trim();
                        nuevaOpcion.textContent = nuevaCat.trim();
                        nuevaOpcion.selected = true;
                        this.insertBefore(nuevaOpcion, nueva);
                        this.value = nuevaCat.trim();
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
    }
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
    const sku = document.getElementById("sku").value;
    const nombre = document.getElementById("nombre").value.trim();
    const descripcion = document.getElementById("descripcion").value.trim();
    const codigoDeBarras = document.getElementById("codigoDeBarras").value.trim();
    const categoria = document.getElementById("categoria").value;

    if (!nombre || !categoria) {
        Swal.fire("Error", "Nombre y Categoría son obligatorios", "warning");
        return;
    }

    const producto = {
        sku,
        nombre,
        descripcion,
        codigoDeBarras,
        categoria: { nombre: categoria }
    };

    const metodo = sku.startsWith("PRD") ? "POST" : "PUT";

    try {
        const res = await fetch("http://localhost:8080/api/productos", {
            method: metodo,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(producto)
        });

        if (res.ok) {
            const mensaje = sku.startsWith("PRD") ? "creado" : "actualizado";
            Swal.fire("Guardado", `Producto ${mensaje} correctamente`, "success");
            cerrarModal();
            await cargarProductos();
        } else {
            throw new Error("Error en guardado");
        }
    } catch (error) {
        Swal.fire("Error", "No se pudo guardar el producto", "error");
    }
}

async function eliminarProducto(sku, nombre) {
    const confirm = await Swal.fire({
        title: "¿Eliminar producto?",
        text: `¿Estás seguro de eliminar el producto "${nombre}"?`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Sí, eliminar"
    });

    if (confirm.isConfirmed) {
        try {
            const res = await fetch(`http://localhost:8080/api/productos/${sku}`, {
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
