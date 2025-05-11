package com.smartmarket.api.services;

import com.smartmarket.api.models.Cliente;
import com.smartmarket.api.repositories.IClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {
    private final IClienteRepository clienteRepository;

    public ClienteService(IClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // Obtener todos los clientes
    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    // Obtener un cliente por ID
    public Optional<Cliente> obtenerPorId(Integer id) {
        return clienteRepository.findById(id);
    }

    // Crear un nuevo cliente
    public Cliente crearCliente(Cliente cliente) {
        if (clienteRepository.existsByEmail(cliente.getEmail())) {
            throw new IllegalArgumentException("El cliente ya existe con ese email");
        }
        return clienteRepository.save(cliente);
    }

    // Crear múltiples clientes (Bulk Insert)
    public List<Cliente> crearClientes(List<Cliente> clientes) {
        for (Cliente cliente : clientes) {
            if (clienteRepository.existsByEmail(cliente.getEmail())) {
                throw new IllegalArgumentException("El cliente ya existe con ese email: " + cliente.getEmail());
            }
        }
        return clienteRepository.saveAll(clientes);
    }

    // Actualizar un cliente existente
    public Cliente actualizarCliente(Integer id, Cliente clienteActualizado) {
        return clienteRepository.findById(id)
                .map(cliente -> {
                    cliente.setNombre(clienteActualizado.getNombre());
                    cliente.setTelefono(clienteActualizado.getTelefono());
                    cliente.setEmail(clienteActualizado.getEmail());
                    return clienteRepository.save(cliente);
                })
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
    }

    // Eliminar un cliente por ID
    public void eliminarCliente(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new IllegalArgumentException("El cliente no existe");
        }
        clienteRepository.deleteById(id);
    }
}