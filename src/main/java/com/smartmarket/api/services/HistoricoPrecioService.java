package com.smartmarket.api.services;

import com.smartmarket.api.models.Producto;
import com.smartmarket.api.models.HistoricoPrecio;
import com.smartmarket.api.repositories.IHistoricoPrecioRepository;
import com.smartmarket.api.repositories.IProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistoricoPrecioService {

    @Autowired
    private IHistoricoPrecioRepository historicoPrecioRepository;

    @Autowired
    private IProductoRepository productoRepository;

    public ResponseEntity<?> listar() {
        List<HistoricoPrecio> lista = historicoPrecioRepository.findAll();
        return ResponseEntity.ok(lista);
    }

    public ResponseEntity<?> obtenerPorId(Integer id) {
        Optional<HistoricoPrecio> historico = historicoPrecioRepository.findById(id);
        if (historico.isPresent()) {
            return ResponseEntity.ok(historico.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Histórico no encontrado");
        }
    }

    public ResponseEntity<?> crear(HistoricoPrecio historicoPrecio) {
        if (historicoPrecio.getProducto() == null || historicoPrecio.getProducto().getSku() == null) {
            return ResponseEntity.badRequest().body("El SKU del producto es obligatorio.");
        }

        Optional<Producto> producto = productoRepository.findBySku(historicoPrecio.getProducto().getSku());
        if (!producto.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado con el SKU proporcionado.");
        }

        historicoPrecio.setProducto(producto.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(historicoPrecioRepository.save(historicoPrecio));
    }

    public ResponseEntity<?> actualizar(Integer id, HistoricoPrecio historicoPrecio) {
        Optional<HistoricoPrecio> existente = historicoPrecioRepository.findById(id);
        if (!existente.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Histórico no encontrado");
        }

        if (historicoPrecio.getProducto() == null || historicoPrecio.getProducto().getSku() == null) {
            return ResponseEntity.badRequest().body("El SKU del producto es obligatorio.");
        }

        Optional<Producto> producto = productoRepository.findBySku(historicoPrecio.getProducto().getSku());
        if (!producto.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado con el SKU proporcionado.");
        }

        historicoPrecio.setId(id);
        historicoPrecio.setProducto(producto.get());
        return ResponseEntity.ok(historicoPrecioRepository.save(historicoPrecio));
    }

    public ResponseEntity<?> eliminar(Integer id) {
        if (!historicoPrecioRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Histórico no encontrado");
        }

        historicoPrecioRepository.deleteById(id);
        return ResponseEntity.ok("Histórico eliminado correctamente");
    }
}
