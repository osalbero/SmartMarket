package com.smartmarket.api.services;

import com.smartmarket.api.models.Cliente;
import com.smartmarket.api.repositories.IClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    // Buscar clientes por nombre, teléfono o email
    public List<Cliente> buscarPorNombreOTelefonoOEmail(String query) {
        return clienteRepository
                .findByNombreContainingIgnoreCaseOrTelefonoContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        query, query, query);
    }

    // Obtener un cliente por ID
    public Optional<Cliente> obtenerPorId(Integer id) {
        return clienteRepository.findById(id);
    }

    // Crear un nuevo cliente
    public Cliente crearCliente(Cliente cliente) {
        if (clienteRepository.existsByEmail(cliente.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un cliente con el email: " + cliente.getEmail());
        }
        return clienteRepository.save(cliente);
    }

    // Crear múltiples clientes (Bulk Insert)
    public List<Cliente> crearClientes(List<Cliente> clientes) {
        for (Cliente cliente : clientes) {
            if (clienteRepository.existsByEmail(cliente.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El cliente ya existe con el email: " + cliente.getEmail());
            }
        }
        return clienteRepository.saveAll(clientes);
    }

    // Actualizar un cliente existente
    public Cliente actualizarCliente(Integer id, Cliente clienteActualizado) {
    return clienteRepository.findById(id)
            .map(cliente -> {
                // Validar si el nuevo email ya está en uso por otro cliente
                Optional<Cliente> clienteExistenteConMismoEmail = clienteRepository.findByEmail(clienteActualizado.getEmail());
                
                if (clienteExistenteConMismoEmail.isPresent() && !clienteExistenteConMismoEmail.get().getId().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otro cliente con el email: " + clienteActualizado.getEmail());
                }

                // Si no hay conflicto, actualizar
                cliente.setNombre(clienteActualizado.getNombre());
                cliente.setTelefono(clienteActualizado.getTelefono());
                cliente.setEmail(clienteActualizado.getEmail());

                return clienteRepository.save(cliente);
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
}


    // Eliminar un cliente por ID
    public void eliminarCliente(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El cliente no existe");
        }
        clienteRepository.deleteById(id);
    }
}
