import {fetchWithAuth} from "../main.js";
// modulos/ventas.js

// Variables globales para almacenar datos cargados y el estado del formulario
let clientesData = [];
let empleadosData = [];
let productRows = [];
let productRowCounter = 0;

// Variable global para almacenar el total de la venta actual
// Aseguramos que la variable esté declarada y visible en todo el módulo.
let currentSaleTotal = 0;

// Función principal para cargar la vista de ventas
export async function cargarVistaVentas() {
    console.log('Iniciando carga de la vista de ventas. currentSaleTotal al inicio:', currentSaleTotal);
    const res = await fetch('./vistas/ventas.html');
    const html = await res.text();
    const contenedor = document.getElementById("contenido");

    contenedor.classList.remove("animacion-entrada");
    void contenedor.offsetHeight;
    contenedor.innerHTML = html;
    contenedor.classList.add("animacion-entrada");

    // Cargar datos iniciales (clientes y empleados)
    clientesData = await cargarDatosAPI("/api/clientes");
    empleadosData = await cargarDatosAPI("/api/empleados");

    // Rellenar selects de cliente y empleado
    rellenarSelect(document.getElementById('clienteSelect'), clientesData, 'Seleccione un cliente');
    rellenarSelect(document.getElementById('empleadoSelect'), empleadosData, 'Seleccione un empleado');

    // Inicializar el formulario con una fila de producto
    productRows = [];
    productRowCounter = 0;
    addProductRow();

    // Asignar event listeners
    document.getElementById('salesForm').addEventListener('submit', handleFormSubmit);

    document.getElementById('btnAddProduct').addEventListener('click', addProductRow);
    
    // Event listeners para el modal de pago
    document.getElementById('btnCancelPayment').addEventListener('click', () => {
        document.getElementById('paymentModal').style.display = 'none';
    });
    document.getElementById('btnProcessPayment').addEventListener('click', processPaymentAndRegisterSale);
    
    // Event listener para el campo de efectivo recibido
    document.getElementById('cashReceived').addEventListener('input', updateChange);
}

// Función auxiliar para cargar datos de APIs (reutilizable)
async function cargarDatosAPI(url) {
    try {
        const res = await fetchWithAuth(url);
        if (!res.ok) throw new Error(`Error al cargar datos de ${url}`);
        return await res.json();
    } catch (e) {
        console.error(`Error al cargar datos de ${url}:`, e);
        Swal.fire("Error", `No se pudieron cargar datos desde ${url}`, "error");
        return [];
    }
}

// Función auxiliar para rellenar un <select>
function rellenarSelect(selectElement, data, defaultOptionText) {
    selectElement.innerHTML = `<option value="">${defaultOptionText}</option>`;
    data.forEach(item => {
        const opt = document.createElement("option");
        opt.value = item.id;
        opt.textContent = item.nombre || item.razonSocial || item.descripcion || `ID: ${item.id}`;
        selectElement.appendChild(opt);
    });
}

// Función para añadir una nueva fila de producto al array productRows
function addProductRow() {
    productRows.push({
        id: productRowCounter++,
        sku: '',
        cantidad: 1,
        precioUnitario: 0,
        stockDisponible: 0,
        isValid: false,
        productName: ''
    });
    renderProductRows();
    updateOverallTotal();
}

// Función para remover una fila de producto del array productRows
function removeProductRow(idToRemove) {
    if (productRows.length === 1) {
        Swal.fire("Advertencia", "Debe haber al menos un producto en la venta.", "warning");
        return;
    }
    productRows = productRows.filter(row => row.id !== idToRemove);
    renderProductRows();
    updateOverallTotal();
}

// Función central para renderizar (o re-renderizar) todos los bloques de producto
function renderProductRows() {
    const container = document.getElementById('productosContainer');
    container.innerHTML = '';

    productRows.forEach((rowData, index) => {
        const rowId = `productRow-${rowData.id}`;
        const productRowHtml = `
            <div class="product-row-block" id="${rowId}">
                <h4>Producto ${index + 1}
                    ${productRows.length > 1 ? `<button type="button" class="btn-remove-product" data-id-to-remove="${rowData.id}">X</button>` : ''}
                </h4>
                <div style="display: flex; gap: 20px; flex-wrap: wrap;">
                    <div class="form-group" style="flex: 1 1 200px;">
                        <label for="sku-${rowData.id}">SKU:</label>
                        <input type="text" id="sku-${rowData.id}" class="input sku-input" style="text-transform: uppercase;" value="${rowData.sku || ''}">
                    </div>
                    <div class="form-group search-container" style="flex: 1 1 300px;">
                        <label for="search-${rowData.id}">Buscar por nombre:</label>
                        <input type="text" id="search-${rowData.id}" class="input search-input" placeholder="Nombre del producto">
                        <ul id="searchResults-${rowData.id}" class="search-results"></ul>
                    </div>
                </div>
                <div style="display: flex; gap: 20px; flex-wrap: wrap; margin-top: 10px; align-items: flex-end;">
                    <div class="form-group" style="flex: 1 1 100px;">
                        <label for="cantidad-${rowData.id}">Cantidad:</label>
                        <input type="number" id="cantidad-${rowData.id}" class="input" required min="1" value="${rowData.cantidad || 1}">
                    </div>
                    <div class="form-group" style="flex: 1 1 auto;">
                        <label>Precio Unitario:</label>
                        <span id="unitPriceDisplay-${rowData.id}" style="font-weight: bold;">${new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(rowData.precioUnitario || 0)}</span>
                    </div>
                    <div class="form-group" style="flex: 1 1 auto;">
                        <label>Total Producto:</label>
                        <span id="rowTotalDisplay-${rowData.id}" style="font-weight: bold;">${new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format((rowData.cantidad || 0) * (rowData.precioUnitario || 0))}</span>
                    </div>
                </div>
                <div style="margin-top: 10px;">
                    <span id="productNamePreview-${rowData.id}" class="product-info-preview" style="font-weight: bold; color: #444;">${rowData.productName || ''}</span>
                    <span id="stockStatus-${rowData.id}" class="stock-status ${rowData.stockStatusClass || ''}">${rowData.stockStatusText || ''}</span>
                </div>
                <hr style="margin: 20px 0;">
            </div>
        `;
        container.insertAdjacentHTML('beforeend', productRowHtml);

        // Re-adjuntar event listeners a los elementos recién creados
        const currentSkuInput = document.getElementById(`sku-${rowData.id}`);
        const currentSearchInput = document.getElementById(`search-${rowData.id}`);
        const currentQtyInput = document.getElementById(`cantidad-${rowData.id}`);
        const currentRemoveButton = document.querySelector(`#${rowId} .btn-remove-product`);

        // Event listener para el campo de SKU
        currentSkuInput.addEventListener('change', () => validateProductSkuAndStock(rowData.id, currentSkuInput.value));

        // Event listener para el campo de búsqueda por nombre
        currentSearchInput.addEventListener('input', debounce(() => {
            const query = currentSearchInput.value.trim();
            if (query.length > 2) {
                searchProductsByName(rowData.id, query);
            } else {
                document.getElementById(`searchResults-${rowData.id}`).innerHTML = '';
            }
        }, 300));
        
        currentSearchInput.addEventListener('focus', () => {
            if (currentSearchInput.value.length > 2) {
                searchProductsByName(rowData.id, currentSearchInput.value.trim());
            }
        });
        
        currentSearchInput.addEventListener('blur', () => {
            // Retrasar el ocultar para permitir el click
            setTimeout(() => {
                document.getElementById(`searchResults-${rowData.id}`).innerHTML = '';
            }, 200);
        });

        // Event listeners para el campo de cantidad
        currentQtyInput.addEventListener('input', () => updateRowTotal(rowData.id));
        currentQtyInput.addEventListener('change', () => validateProductSkuAndStock(rowData.id, currentSkuInput.value));

        // Event listener para el botón de eliminar
        if (currentRemoveButton) {
            currentRemoveButton.addEventListener('click', () => removeProductRow(rowData.id));
        }
    });
}

// Función debounce para retrasar la ejecución de la búsqueda
function debounce(func, delay) {
    let timeout;
    return function(...args) {
        const context = this;
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(context, args), delay);
    };
}

// Función para buscar productos por nombre y mostrar resultados
async function searchProductsByName(rowId, query) {
    const searchResultsList = document.getElementById(`searchResults-${rowId}`);
    searchResultsList.innerHTML = '';
    
    try {
        const resp = await fetchWithAuth(`/api/productos/buscar?query=${encodeURIComponent(query)}`);
        if (!resp.ok) throw new Error("Error al buscar productos");
        const productos = await resp.json();

        if (productos.length === 0) {
            searchResultsList.innerHTML = `<li style="padding: 10px; color: #888;">No se encontraron resultados.</li>`;
        } else {
            productos.forEach(p => {
                const li = document.createElement('li');
                li.textContent = `${p.nombre} (SKU: ${p.sku})`;
                li.setAttribute('data-sku', p.sku);
                li.setAttribute('data-name', p.nombre);
                li.addEventListener('mousedown', () => {
                    const skuInput = document.getElementById(`sku-${rowId}`);
                    const searchInput = document.getElementById(`search-${rowId}`);
                    skuInput.value = p.sku;
                    searchInput.value = p.nombre;
                    searchResultsList.innerHTML = '';
                    validateProductSkuAndStock(rowId, p.sku);
                });
                searchResultsList.appendChild(li);
            });
        }
    } catch (error) {
        console.error("Error en la búsqueda:", error);
        searchResultsList.innerHTML = `<li style="padding: 10px; color: red;">Error en la búsqueda.</li>`;
    }
}

// Función para validar SKU y stock
async function validateProductSkuAndStock(rowId, sku) {
    const rowIndex = productRows.findIndex(row => row.id === rowId);
    if (rowIndex === -1) return;

    const rowData = productRows[rowIndex];
    const skuInput = document.getElementById(`sku-${rowData.id}`);
    const qtyInput = document.getElementById(`cantidad-${rowData.id}`);
    const productNamePreview = document.getElementById(`productNamePreview-${rowData.id}`);
    const stockStatus = document.getElementById(`stockStatus-${rowData.id}`);
    const unitPriceDisplay = document.getElementById(`unitPriceDisplay-${rowData.id}`);
    const searchInput = document.getElementById(`search-${rowData.id}`);

    const cantidad = parseInt(qtyInput.value);

    // Limpiar estados previos y actualizar rowData
    rowData.sku = sku;
    rowData.cantidad = cantidad;
    rowData.productName = '';
    rowData.precioUnitario = 0;
    rowData.stockDisponible = 0;
    rowData.isValid = false;
    rowData.stockStatusText = '';
    rowData.stockStatusClass = '';

    productNamePreview.textContent = '';
    stockStatus.textContent = '';
    stockStatus.className = 'stock-status';
    unitPriceDisplay.textContent = '$ 0.00';
    skuInput.classList.remove('invalid');
    searchInput.classList.remove('invalid');

    if (sku === '') {
        searchInput.value = '';
        updateRowTotal(rowData.id);
        return;
    }

    try {
        const resp = await fetchWithAuth(`/api/productos/sku/${sku}`);
        if (!resp.ok) {
            throw new Error(`Producto no encontrado con SKU: ${sku}`);
        }
        const producto = await resp.json();

        rowData.productName = producto.nombre;
        rowData.precioUnitario = producto.precio;
        rowData.stockDisponible = producto.stock;

        productNamePreview.textContent = rowData.productName;
        unitPriceDisplay.textContent = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(rowData.precioUnitario);
        searchInput.value = rowData.productName;

        // Validar stock
        if (cantidad > rowData.stockDisponible) {
            rowData.stockStatusText = `Stock insuficiente (${rowData.stockDisponible} disponibles)`;
            rowData.stockStatusClass = 'stock-none';
            skuInput.classList.add('invalid');
            searchInput.classList.add('invalid');
            Swal.fire("Advertencia", `Cantidad solicitada (${cantidad}) excede el stock disponible (${rowData.stockDisponible}) para ${rowData.productName}.`, "warning");
        } else if (rowData.stockDisponible > 0 && cantidad <= rowData.stockDisponible) {
            rowData.stockStatusText = `Stock: ${rowData.stockDisponible}`;
            rowData.stockStatusClass = 'stock-ok';
            rowData.isValid = true;
        } else {
            rowData.stockStatusText = 'Sin stock';
            rowData.stockStatusClass = 'stock-none';
            skuInput.classList.add('invalid');
            searchInput.classList.add('invalid');
            Swal.fire("Advertencia", `El producto ${rowData.productName} no tiene stock disponible.`, "warning");
        }

    } catch (error) {
        console.error("Error al validar SKU:", error);
        rowData.productName = "SKU no válido o no encontrado";
        rowData.stockStatusText = '';
        productNamePreview.textContent = rowData.productName;
        productNamePreview.style.color = "red";
        skuInput.classList.add('invalid');
        searchInput.classList.add('invalid');
        Swal.fire("Error", error.message, "error");
    } finally {
        stockStatus.textContent = rowData.stockStatusText;
        stockStatus.className = `stock-status ${rowData.stockStatusClass}`;
        
        updateRowTotal(rowData.id);
        updateOverallTotal();
    }
}

// Función para actualizar el total de una fila de producto
function updateRowTotal(rowId) {
    const rowIndex = productRows.findIndex(row => row.id === rowId);
    if (rowIndex === -1) return;

    const rowData = productRows[rowIndex];
    const rowTotalDisplay = document.getElementById(`rowTotalDisplay-${rowData.id}`);

    const cantidad = parseInt(document.getElementById(`cantidad-${rowData.id}`).value);
    
    rowData.cantidad = cantidad;

    if (isNaN(cantidad) || cantidad <= 0 || !rowData.isValid || cantidad > rowData.stockDisponible) {
        rowTotalDisplay.textContent = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(0);
        updateOverallTotal();
        return;
    }

    const total = cantidad * rowData.precioUnitario;
    rowTotalDisplay.textContent = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(total);
    updateOverallTotal();
}

// Función para actualizar el total general de la venta
function updateOverallTotal() {
    let grandTotal = 0;
    productRows.forEach(row => {
        if (row.isValid && row.cantidad > 0 && row.cantidad <= row.stockDisponible) {
            grandTotal += (row.cantidad * row.precioUnitario);
        }
    });
    document.getElementById('saleTotalAmount').textContent = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(grandTotal);
    currentSaleTotal = grandTotal; // Guardar el total en la variable global
}

// Función que se ejecuta al presionar el botón "Pagar" del formulario
async function handleFormSubmit(e) {
    e.preventDefault();
    
    // Aquí actualizamos el total por última vez antes de abrir el modal
    updateOverallTotal();

    const clienteId = document.getElementById('clienteSelect').value;
    const empleadoId = document.getElementById('empleadoSelect').value;

    if (!clienteId || !empleadoId) {
        Swal.fire("Advertencia", "Por favor, seleccione un cliente y un empleado.", "warning");
        return;
    }

    let allProductsAreValid = true;
    productRows.forEach(row => {
        if (!row.sku || row.cantidad <= 0 || !row.isValid || row.cantidad > row.stockDisponible) {
            allProductsAreValid = false;
        }
    });

    if (!allProductsAreValid) {
        Swal.fire("Advertencia", "Algunos productos en la venta son inválidos o tienen stock insuficiente. Por favor, revise.", "warning");
        return;
    }
    
    // Nueva validación: si la venta tiene un total de 0, no se puede pagar
    if (currentSaleTotal <= 0) {
        Swal.fire("Advertencia", "El total de la venta debe ser mayor a cero para proceder con el pago.", "warning");
        return;
    }

    // Si todo es válido, mostramos el modal de pago
    showPaymentModal();
}

// Función para mostrar el modal de pago
function showPaymentModal() {
    console.log('Mostrando modal de pago. Total de venta:', currentSaleTotal);
    document.getElementById('paymentModal').style.display = 'flex';
    document.getElementById('modalTotalAmount').value = currentSaleTotal.toFixed(2);
    document.getElementById('cashReceived').value = '';
    document.getElementById('changeAmount').value = '0.00';
    document.querySelector('input[name="paymentType"][value="cash"]').checked = true; // Seleccionar efectivo por defecto
    
    updateChange();
}

// Función para actualizar el cambio
function updateChange() {
    const total = currentSaleTotal;
    const cashReceived = parseFloat(document.getElementById('cashReceived').value) || 0;
    let change = cashReceived - total;

    if (change < 0) {
        change = 0;
        document.getElementById('btnProcessPayment').disabled = true;
        document.getElementById('btnProcessPayment').style.opacity = '0.5';
        document.getElementById('btnProcessPayment').style.cursor = 'not-allowed';
    } else {
        document.getElementById('btnProcessPayment').disabled = false;
        document.getElementById('btnProcessPayment').style.opacity = '1';
        document.getElementById('btnProcessPayment').style.cursor = 'pointer';
    }

    document.getElementById('changeAmount').value = change.toFixed(2);
}

// Función para procesar el pago y registrar la venta (nueva lógica)
async function processPaymentAndRegisterSale() {
    const clienteId = document.getElementById('clienteSelect').value;
    const empleadoId = document.getElementById('empleadoSelect').value;
    const paymentType = document.querySelector('input[name="paymentType"]:checked').value;

    const cashReceived = parseFloat(document.getElementById('cashReceived').value) || 0;
    if (paymentType === 'cash' && cashReceived < currentSaleTotal) {
        Swal.fire("Advertencia", "El efectivo recibido es insuficiente para cubrir el total de la venta.", "warning");
        return;
    }

    const productosParaVenta = productRows.map(row => ({
        sku: row.sku,
        cantidad: row.cantidad
    }));

    const ventaData = {
        Idcliente: parseInt(clienteId),
        Idempleado: parseInt(empleadoId),
        productos: productosParaVenta,
        metodoPago: paymentType
    };

    console.log("Datos de la venta a enviar:", JSON.stringify(ventaData, null, 2));

    try {
        const url = "/api/ventas";
        const res = await fetchWithAuth(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(ventaData)
        });

        if (res.ok) {
            Swal.fire("Venta Registrada", "La venta se ha registrado exitosamente.", "success");
            document.getElementById('paymentModal').style.display = 'none';
            resetSalesForm();
        } else {
            const errorText = await res.text();
            console.error("Error al registrar venta:", errorText);
            Swal.fire("Error", `No se pudo registrar la venta: ${errorText}`, "error");
        }
    } catch (error) {
        console.error("Error de red al registrar venta:", error);
        Swal.fire("Error", "Ocurrió un error al intentar conectar con el servidor.", "error");
    }
}

// Función para reiniciar el formulario de ventas
function resetSalesForm() {
    document.getElementById('salesForm').reset();
    productRows = [];
    productRowCounter = 0;
    addProductRow();
    updateOverallTotal();
}
