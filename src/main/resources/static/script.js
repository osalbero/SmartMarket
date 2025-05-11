document.addEventListener("DOMContentLoaded", () => {
    // Botón de Productos
    document.getElementById("menu-productos").addEventListener("click", (e) => {
        e.preventDefault();
        cargarVistaProductos();
    });

    // Botón de Clientes
    document.getElementById("menu-clientes").addEventListener("click", (e) => {
        e.preventDefault();
        cargarVistaClientes();
    });

    // Botón "Agregar" Producto
    const btnAgregar = document.getElementById("btnAgregar");
    if (btnAgregar) {
        btnAgregar.addEventListener("click", () => {
            document.getElementById("modalProducto").classList.remove("hidden");
        });
    }

    // Botón "Menú" para volver a la página de inicio
    const btnInicio = document.getElementById("menu-inicio");
    if (btnInicio) {
        btnInicio.addEventListener("click", (e) => {
            e.preventDefault();
            mostrarInicio();
        });
    }
    
    mostrarInicio(); // Mostrar la página de inicio al cargar


});

// Función para mostrar la página de inicio
function mostrarInicio() {
    const contenido = document.getElementById("contenido");
    contenido.innerHTML = `
        <div class="welcome-card">
    <div class="icon">📈</div>
    <h1>Bienvenido a SmartMarket</h1>
    <p>Gestiona productos, ventas, clientes y mucho más desde un solo lugar.</p>
    </div>
    `;
}


function editarProducto(producto) {
    abrirModal(producto);
}

// === MÓDULO PRODUCTOS ===
async function cargarVistaProductos() {
    const contenedor = document.getElementById("contenido");
    contenedor.innerHTML = `
        <h2>Gestión de Productos</h2>
<button id="btnAgregar" class="btn-agregar">Agregar Producto</button>

<div class="tabla-contenedor">
    <table class="tabla-productos">
        <thead>
            <tr>
                <th>SKU</th>
                <th>Nombre</th>
                <th>Descripción</th>
                <th>Código de Barras</th>
                <th>Categoría</th>
                <th>Acciones</th>
            </tr>
        </thead>
        <tbody id="tablaProductosBody"></tbody>
    </table>
</div>

<!-- Modal con mejor diseño -->
<div id="modalProducto" class="modal hidden">
    <div class="modal-content">
        <span class="cerrar-modal" onclick="cerrarModal()">×</span>
        <h3 id="tituloModal">Agregar Producto</h3>

        <form id="formProducto">
            <div class="form-group">
                <label for="sku">SKU:</label>
                <input type="text" id="sku" placeholder="Ingrese el SKU" required>
            </div>

            <div class="form-group">
                <label for="nombre">Nombre:</label>
                <input type="text" id="nombre" placeholder="Ingrese Nombre del Producto" required>
            </div>

            <div class="form-group">
                <label for="descripcion">Descripción:</label>
                <input type="text" id="descripcion" placeholder="Ingrese una breve descripción">
            </div>

            <div class="form-group">
                <label for="codigoDeBarras">Código de Barras:</label>
                <input type="text" id="codigoDeBarras" placeholder="Ingrese Código de Barras">
            </div>

            <div class="form-group">
                <label for="categoria">Categoría:</label>
                <select id="categoria">
                    <option value="">Seleccione una categoría</option>
                    <option value="Granos">Granos</option>
                    <option value="Lácteos">Lácteos</option>
                    <option value="Snacks">Snacks</option>
                    <option value="Aseo Personal">Aseo Personal</option>
                </select>
            </div>

            <div class="button-container">
                <button type="submit" class="btn-guardar">Guardar Producto</button>
            </div>
        </form>
    </div>
</div>

    `;

    await cargarProductos();
    document.getElementById("btnAgregar").addEventListener("click", () => abrirModal());
    document.getElementById("formProducto").addEventListener("submit", guardarProducto);
}

async function cargarProductos() {
    try {
        const response = await fetch("http://localhost:8080/api/productos");
        if (!response.ok) throw new Error("Error de red o del servidor");

        const productos = await response.json();
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
            <td class="acciones">
                <button class="btn-editar" data-producto='${encodeURIComponent(JSON.stringify(p))}'>Editar</button>
                <button class="btn-eliminar" data-sku='${p.sku}'>Eliminar</button>
            </td>
        `;

        body.appendChild(fila);
    });

    agregarEventosBotones();
}

function agregarEventosBotones() {
    document.querySelectorAll('.btn-editar').forEach(btn => {
        btn.addEventListener('click', () => {
            const producto = JSON.parse(decodeURIComponent(btn.getAttribute('data-producto')));
            abrirModal(producto);
        });
    });

    document.querySelectorAll('.btn-eliminar').forEach(btn => {
        btn.addEventListener('click', () => {
            const sku = btn.getAttribute('data-sku');
            eliminarProducto(sku);
        });
    });
}





function abrirModal(producto = null) {
    document.getElementById("tituloModal").innerText = producto ? "Editar Producto" : "Agregar Producto";
    document.getElementById("formProducto").reset();

    const campoSku = document.getElementById("sku");

    if (producto) {
        campoSku.value = producto.sku;
        campoSku.readOnly = true; // ✅ Bloquear SKU si se está editando

        document.getElementById("nombre").value = producto.nombre;
        document.getElementById("descripcion").value = producto.descripcion;
        document.getElementById("codigoDeBarras").value = producto.codigoDeBarras;
        cargarCategorias(producto.categoria?.nombre);
    } else {
        campoSku.value = "";
        campoSku.readOnly = false; // ✅ Permitir edición de SKU si es nuevo
        cargarCategorias();
    }

    document.getElementById("modalProducto").classList.remove("hidden");
}



function cerrarModal() {
    document.getElementById("modalProducto").classList.add("hidden");
}

async function cargarCategorias(seleccionada = "") {
    const response = await fetch("http://localhost:8080/api/categorias");
    const categorias = await response.json();
    const select = document.getElementById("categoria");

    select.innerHTML = "";
    select.onchange = null;

    categorias.forEach(cat => {
        const option = document.createElement("option");
        option.value = cat.nombre;
        option.textContent = cat.nombre;
        if (cat.nombre === seleccionada) {
            option.selected = true;
        }
        select.appendChild(option);
    });

    const nuevaOption = document.createElement("option");
    nuevaOption.value = "__nueva__";
    nuevaOption.textContent = "+ Crear nueva categoría";
    select.appendChild(nuevaOption);

    select.addEventListener("change", async function () {
        if (this.value === "__nueva__") {
            const { value: nueva } = await Swal.fire({
                title: 'Nueva Categoría',
                input: 'text',
                inputLabel: 'Ingrese el nombre de la nueva categoría',
                showCancelButton: true,
                inputValidator: (value) => {
                    if (!value) return "El nombre es obligatorio";
                    return null;
                }
            });

            if (nueva) {
                // Validación local para evitar duplicados (ignorando mayúsculas/minúsculas)
                const yaExiste = Array.from(select.options).some(opt =>
                    opt.value.toLowerCase() === nueva.trim().toLowerCase()
                );

                if (yaExiste) {
                    Swal.fire("Categoría duplicada", "Ya existe una categoría con ese nombre", "warning");
                    this.value = "";
                    return;
                }

                // Enviar al backend
                const res = await fetch("http://localhost:8080/api/categorias", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ nombre: nueva.trim() })
                });

                if (res.ok) {
                    const nuevaOpcion = document.createElement("option");
                    nuevaOpcion.value = nueva.trim();
                    nuevaOpcion.textContent = nueva.trim();
                    nuevaOpcion.selected = true;
                    this.insertBefore(nuevaOpcion, nuevaOption);
                    this.value = nueva.trim();
                    Swal.fire("Categoría creada", "", "success");
                } else {
                    Swal.fire("Error", "No se pudo guardar la categoría", "error");
                    this.value = "";
                }
            } else {
                this.value = "";
            }
        }
    });
}




async function guardarProducto(e) {
    e.preventDefault();

    const sku = document.getElementById("sku").value; // SKU del producto
    const creando = !document.getElementById("sku").readOnly || !sku; // Verifica si es un nuevo producto
    const producto = {
        sku: sku,
        nombre: document.getElementById("nombre").value,
        descripcion: document.getElementById("descripcion").value,
        codigoDeBarras: document.getElementById("codigoDeBarras").value,
        categoria: { nombre: document.getElementById("categoria").value }
    };

    const metodo = creando ? "POST" : "PUT";
    const url = creando
        ? "http://localhost:8080/api/productos"
        : `http://localhost:8080/api/productos/sku/${sku}`;

    const res = await fetch(url, {
        method: metodo,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(producto)
    });

    if (res.ok) {
        Swal.fire("Éxito", "Producto guardado correctamente", "success");
        cerrarModal();
        cargarProductos();
    } else {
        Swal.fire("Error", "No se pudo guardar el producto", "error");
    }
}

async function eliminarProducto(sku) {
    const confirmacion = await Swal.fire({
        title: "¿Estás seguro?",
        text: "Esta acción eliminará el producto.",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Sí, eliminar"
    });

    if (confirmacion.isConfirmed) {
        const res = await fetch(`http://localhost:8080/api/productos/${sku}`, {
            method: "DELETE"
        });
        if (res.ok) {
            Swal.fire("Eliminado", "Producto eliminado con éxito", "success");
            cargarProductos();
        } else {
            Swal.fire("Error", "No se pudo eliminar", "error");
        }
    }
}

// Captura todos los enlaces del menú
const menuItems = document.querySelectorAll('.sidebar ul li a');

menuItems.forEach(item => {
    item.addEventListener('click', function (e) {
        e.preventDefault(); // Evita recargar la página

        // Quita la clase 'active' de todos los enlaces
        menuItems.forEach(i => i.classList.remove('active'));

        // Agrega la clase 'active' al enlace clicado
        this.classList.add('active');

        // Cargar el módulo correspondiente en #contenido (ejemplo básico)
        cargarModulo(this.id);
    });
});

// Función de ejemplo para cargar módulos dinámicamente
function cargarModulo(menuId) {
    const contenido = document.getElementById('contenido');
    switch (menuId) {
        case 'menu-productos':
            contenido.innerHTML = '<h1>Gestión de Productos</h1>';
            break;
        case 'menu-categorias':
            contenido.innerHTML = '<h1>Gestión de Categorías</h1>';
            break;
        case 'menu-proveedores':
            contenido.innerHTML = '<h1>Gestión de Proveedores</h1>';
            break;
        case 'menu-clientes':
            contenido.innerHTML = '<h1>Gestión de Clientes</h1>';
            break;
        case 'menu-ventas':
            contenido.innerHTML = '<h1>Gestión de Ventas</h1>';
            break;
        case 'menu-compras':
            contenido.innerHTML = '<h1>Gestión de Compras</h1>';
            break;
        case 'menu-reportes':
            contenido.innerHTML = '<h1>Reportes</h1>';
            break;
        case 'menu-configuraciones':
            contenido.innerHTML = '<h1>Configuración</h1>';
            break;
        case 'menu-cierre-caja':
            contenido.innerHTML = '<h1>Cierre de Caja</h1>';
            break;
        case 'menu-empleados':
            contenido.innerHTML = '<h1>Gestión de Empleados</h1>';
            break;
        default:
            contenido.innerHTML = '<h1>Módulo no encontrado</h1>';
    }
}
