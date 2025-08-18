import {fetchWithAuth} from "../main.js";
// Variable global para mantener el conteo de los movimientos agregados
let movimientoCount = 0;
// Variables para almacenar las listas de proveedores y empleados una vez cargadas
let proveedoresData = [];
let empleadosData = [];

export async function cargarVistaMovimientosInventario() {
    const res = await fetch('./vistas/movimientos_inventario.html');
    const html = await res.text();
    const contenedor = document.getElementById("contenido");

    contenedor.classList.remove("animacion-entrada");
    void contenedor.offsetHeight;
    contenedor.innerHTML = html;
    contenedor.classList.add("animacion-entrada");

    await cargarMovimientos();
    // Cargar proveedores y empleados una sola vez al inicio
    proveedoresData = await cargarDatosAPI("/api/proveedores");
    empleadosData = await cargarDatosAPI("/api/empleados");

    document.getElementById("btnAgregar").addEventListener("click", () => abrirModal());
    // El evento 'submit' ahora va a `guardarLoteMovimientos`
    document.getElementById("formMovimiento").addEventListener("submit", guardarLoteMovimientos);
    document.getElementById("cerrarModal").addEventListener("click", cerrarModal);
    // Nuevo evento para añadir más movimientos dentro del modal
    document.getElementById("btnAgregarMovimientoModal").addEventListener("click", () => addMovimientoFormBlock());

    // Manejo de búsqueda (sin cambios)
    document.getElementById("busqueda").addEventListener("input", async function () {
        const texto = this.value.trim();

        if (texto === "") {
            await cargarMovimientos();
            return;
        }

        try {
            const response = await fetchWithAuth(`/api/movimientos-inventario/buscar?query=${encodeURIComponent(texto)}`);
            if (!response.ok) throw new Error("Error al buscar movimientos");
            const resultados = await response.json();
            renderizarTablaMovimientos(resultados);
        } catch (error) {
            console.error("Error al buscar movimientos:", error);
            Swal.fire("Error", "No se pudo buscar", "error");
        }
    });
}

// Función auxiliar para cargar datos de APIs (proveedores, empleados)
async function cargarDatosAPI(url) {
    try {
        const res = await fetchWithAuth(url);
        if (!res.ok) throw new Error(`Error al cargar datos de ${url}`);
        return await res.json();
    } catch (e) {
        console.error(`Error al cargar datos de ${url}:`, e);
        return [];
    }
}

// Función para generar un bloque de formulario para un solo movimiento
function createMovimientoFormBlock(index) {
    const blockId = `movimientoBlock-${index}`;
    const block = document.createElement('div');
    block.className = 'movimiento-form-block'; // Clase para aplicar estilos si es necesario
    block.id = blockId;
    block.innerHTML = `
        <h4 style="margin-top: 15px; margin-bottom: 10px;">Movimiento ${index + 1}
            ${index > 0 ? `<button type="button" class="btn-remover-movimiento" data-block-id="${blockId}" style="float: right;">X</button>` : ''}
        </h4>
        <div class="form-group">
            <label for="skuMovimiento-${index}">SKU:</label>
            <input type="text" id="skuMovimiento-${index}" class="input sku-input" style="text-transform: uppercase;" required>
            <span id="nombreProductoPreview-${index}" style="margin-left: 10px; font-weight: bold; color: green"></span>
        </div>

        <div class="form-group">
            <label for="tipoMovimiento-${index}">Tipo de Movimiento:</label>
            <select id="tipoMovimiento-${index}" class="input" required>
                <option value="">Seleccione...</option>
                <option value="ENTRADA">ENTRADA</option>
                <option value="SALIDA">SALIDA</option>
            </select>
        </div>

        <div class="form-group">
            <label for="cantidad-${index}">Cantidad:</label>
            <input type="number" id="cantidad-${index}" class="input" required min="1">
        </div>

        <div class="form-group">
            <label for="valorUnitario-${index}">Valor Unitario:</label>
            <input type="number" id="valorUnitario-${index}" class="input" required min="0" step="0.01">
        </div>
        <hr style="margin: 20px 0;">
    `;

        // Añadir event listeners para este nuevo bloque
    const skuInput = block.querySelector(`#skuMovimiento-${index}`);
    skuInput.addEventListener("input", function () {
        this.value = this.value.toUpperCase();
    });
    skuInput.addEventListener("change", () => validarsku(skuInput, block.querySelector(`#nombreProductoPreview-${index}`)));
    skuInput.addEventListener("keydown", function (e) {
        if (e.key === "Enter") {
            e.preventDefault();
            validarsku(skuInput, block.querySelector(`#nombreProductoPreview-${index}`));
        }
    });

    // Event listener para el botón de remover
    const removeBtn = block.querySelector('.btn-remover-movimiento');
    if (removeBtn) {
        removeBtn.addEventListener('click', (event) => {
            const blockToRemoveId = event.target.dataset.blockId;
            document.getElementById(blockToRemoveId).remove();
            // Reajustar los números de los títulos de cada bloque
            updateMovimientoBlockTitles();
        });
    }

    return block;
}

// Función para añadir un nuevo bloque de formulario de movimiento al modal
function addMovimientoFormBlock() {
    const container = document.getElementById('movimientosContainer');
    const newBlock = createMovimientoFormBlock(movimientoCount);
    container.appendChild(newBlock);
    movimientoCount++;
    // Opcional: Desplazar el modal hacia abajo para ver el nuevo bloque
    container.scrollTop = container.scrollHeight;
}

// Función para actualizar los títulos de los bloques después de remover uno
function updateMovimientoBlockTitles() {
    const blocks = document.querySelectorAll('.movimiento-form-block');
    blocks.forEach((block, index) => {
        const titleElement = block.querySelector('h4');
        if (titleElement) {
            titleElement.innerHTML = `Movimiento ${index + 1}
                ${index > 0 ? `<button type="button" class="btn-remover-movimiento" data-block-id="${block.id}" style="float: right;">X</button>` : ''}
            `;
            // Re-añadir event listener si el botón de remover fue recreado
            const removeBtn = titleElement.querySelector('.btn-remover-movimiento');
            if (removeBtn) {
                removeBtn.addEventListener('click', (event) => {
                    const blockToRemoveId = event.target.dataset.blockId;
                    document.getElementById(blockToRemoveId).remove();
                    updateMovimientoBlockTitles(); // Actualizar de nuevo
                });
            }
        }
    });
}


// Abre el modal y reinicia/prepara el formulario para un nuevo lote
function abrirModal() {
    document.getElementById("tituloModal").textContent = "Registrar Movimientos por Lote";
    const form = document.getElementById("formMovimiento");
    form.reset(); // Limpia los valores de los campos

    const movimientosContainer = document.getElementById('movimientosContainer');
    movimientosContainer.innerHTML = ''; // Limpia cualquier bloque anterior

    // Añadir el campo de Número de factura global aquì
    const facturaInputHtml = `
        <div class="form-group" style="margin-bottom: 25px;">
            <label for="numeroFacturaGlobal">Número de Factura:</label>
            <input type="text" id="numeroFacturaGlobal" class="input" placeholder="Ingrese el número de factura">
        </div>

        <div class="form-group" style="margin-bottom: 20px;">
            <label for="proveedorGlobal">Proveedor:</label>
            <select id="proveedorGlobal" class="input"></select>
        </div>

        <div class="form-group" style="margin-bottom: 25px;">
            <label for="empleadoGlobal">Empleado:</label>
            <select id="empleadoGlobal" class="input" required></select>
        </div>
        <hr style="margin: 20px 0;">
    `;
    movimientosContainer.insertAdjacentHTML('beforebegin', facturaInputHtml); // Inserta antes de los bloques de movimientos

    // Rellenar las opciones de los select de proveedores y empleados
    const proveedorGlobalSelect = document.getElementById("proveedorGlobal");
    const empleadoGlobalSelect = document.getElementById("empleadoGlobal");

    proveedorGlobalSelect.innerHTML = '<option value="">Seleccione...</option>';
    proveedoresData.forEach(p => {
        const opt = document.createElement("option");
        opt.value = p.id;
        opt.textContent = p.nombre;
        proveedorGlobalSelect.appendChild(opt);
    });

    empleadoGlobalSelect.innerHTML = '<option value="">Seleccione...</option>';
    empleadosData.forEach(emp => {
        const opt = document.createElement("option");
        opt.value = emp.id;
        opt.textContent = emp.nombre;
        empleadoGlobalSelect.appendChild(opt);
    });

    movimientoCount = 0; // Reinicia el contador
    addMovimientoFormBlock(); // Añade el primer bloque de formulario

    document.getElementById("modalMovimiento").classList.remove("hidden");
}

function cerrarModal() {
    document.getElementById("modalMovimiento").classList.add("hidden");
}

// Función validarsku adaptada para recibir los elementos específicos
async function validarsku(skuInput, nombrePreviewElement) {
    const sku = skuInput.value.trim();
    if (sku === "") {
        nombrePreviewElement.textContent = "";
        return;
    }

    try {
        const resp = await fetchWithAuth(`/api/productos/sku/${sku}`);
        if (!resp.ok) {
            throw new Error(`Producto no encontrado con SKU: ${sku}`);
        }
        const producto = await resp.json();
        console.log("Producto válido:", producto.nombre);
        nombrePreviewElement.textContent = producto.nombre;
        nombrePreviewElement.style.color = "green";
    } catch (error) {
        console.error("Error al validar SKU:", error);
        nombrePreviewElement.textContent = "SKU no válido o no encontrado";
        nombrePreviewElement.style.color = "red";
        Swal.fire("Error", error.message, "error");
    }
}

// Nueva función para guardar un lote de movimientos
async function guardarLoteMovimientos(e) {
    e.preventDefault();

    const movimientos = [];
    const numeroFacturaGlobal = document.getElementById("numeroFacturaGlobal").value.trim();
    const proveedorGlobalId = document.getElementById("proveedorGlobal").value; // Captura ID del proveedor global
    const empleadoGlobalId = document.getElementById("empleadoGlobal").value;   // Captura ID del empleado global

    const movimientoBlocks = document.querySelectorAll('.movimiento-form-block');

    // Validación para los campos globales (especialmente empleado que es requerido)
    if (!empleadoGlobalId) {
        Swal.fire("Advertencia", "Debe seleccionar un empleado para el lote de movimientos.", "warning");
        return;
    }

    for (let i = 0; i < movimientoBlocks.length; i++) {
        const block = movimientoBlocks[i];
        const sku = block.querySelector(`#skuMovimiento-${i}`).value.trim();
        const tipoMovimiento = block.querySelector(`#tipoMovimiento-${i}`).value;
        const cantidad = parseInt(block.querySelector(`#cantidad-${i}`).value.trim());
        const valorUnitario = parseFloat(block.querySelector(`#valorUnitario-${i}`).value.trim());

        // Validación mínima por cada movimiento
        if (!sku || !tipoMovimiento || isNaN(cantidad) || isNaN(valorUnitario) || cantidad <= 0 || valorUnitario < 0) {
            Swal.fire("Advertencia", `Hay un movimiento incompleto o inválido en el bloque ${i + 1}. Por favor, revise todos los campos.`, "warning");
            return; // Detiene el envío si hay un error en cualquier movimiento
        }

        const movimiento = {
            sku,
            numeroFactura: numeroFacturaGlobal || null, // Asignar el número de factura global
            tipoMovimiento,
            cantidad,
            valorUnitario,
            proveedor: proveedorGlobalId ? { id: parseInt(proveedorGlobalId) } : null, // Asignar proveedor global
            empleado: empleadoGlobalId ? { id: parseInt(empleadoGlobalId) } : null   // Asignar empleado global
        };
        movimientos.push(movimiento);
    }

    if (movimientos.length === 0) {
        Swal.fire("Advertencia", "No se ha añadido ningún movimiento para guardar.", "warning");
        return;
    }

    console.log("Guardando lote de movimientos:", JSON.stringify(movimientos, null, 2));

    const url = "/api/movimientos-inventario/lote"; // Endpoint de lote
    try {
        const res = await fetchWithAuth(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(movimientos)
        });

        if (res.ok) {
            Swal.fire("Guardado", "Lote de movimientos registrado correctamente", "success");
            cerrarModal();
            await cargarMovimientos();
        } else {
            const errorText = await res.text();
            console.error("Error en respuesta:", errorText);
            Swal.fire("Error", `No se pudo guardar el lote de movimientos: ${errorText}`, "error");
        }
    } catch (error) {
        console.error("Error al guardar lote de movimientos:", error);
        Swal.fire("Error", "No se pudo guardar el lote de movimientos", "error");
    }
}


async function cargarMovimientos() {
    try {
        const res = await fetchWithAuth("/api/movimientos-inventario");
        if (!res.ok) throw new Error("Error al cargar movimientos");
        const movimientos = await res.json();
        if (!Array.isArray(movimientos)) throw new Error("Datos de movimientos inválidos");
        renderizarTablaMovimientos(movimientos);
    } catch (error) {
        console.error("Error al cargar movimientos:", error);
        Swal.fire("Error", "No se pudieron cargar los movimientos", "error");
    }
}

function renderizarTablaMovimientos(movimientos) {
    const body = document.getElementById("tablaMovimientosBody");
    const termino = document.getElementById("busqueda").value.trim().toLowerCase();
    // Resaltar coincidencias en la búsqueda
    body.innerHTML = "";

    const resaltar = (texto) => {
        if (!termino) return texto;
        const textoStr = String(texto ?? '');
        const regex = new RegExp(`(${termino})`, "gi");
        return textoStr.replace(regex, '<span class="resaltado">$1</span>');
    };
    const formatearMoneda = (valor) => {
        if (isNaN(valor)) return '-';
        return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(valor);
    };

    movimientos.forEach(mov => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
            <td class="oculto">${mov.id}</td>
            <td>${new Date(mov.fechaMovimiento).toLocaleDateString()}</td>
            <td>${resaltar(mov.sku)}</td>
            <td>${resaltar(mov.numeroFactura || '-')}</td>
            <td>${resaltar(mov.producto?.nombre || '-')}</td>
            <td>${resaltar(mov.tipoMovimiento)}</td>
            <td>${resaltar(mov.cantidad)}</td>
            <td>${resaltar(formatearMoneda(mov.valorUnitario))}</td>
            <td>${resaltar(formatearMoneda(mov.valorTotal))}</td>
            <td>${resaltar(mov.proveedor?.nombre || '-')}</td>
            <td>${resaltar(mov.empleado?.nombre || '-')}</td>
            <td></td>
        `;
        body.appendChild(fila);
    });
}

