package com.comidasrapidas.service;

import com.comidasrapidas.exception.ClienteNoEncontradoException;
import com.comidasrapidas.exception.DatosInvalidosException;
import com.comidasrapidas.model.Cliente;
import com.comidasrapidas.repository.ClienteRepository;
import com.comidasrapidas.repository.VentaRepository;
import com.comidasrapidas.util.Validacion;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {
    private final ClienteRepository clientes;
    private final VentaRepository ventas;
    private final SesionService sesion;

    public ClienteService(ClienteRepository clientes, VentaRepository ventas, SesionService sesion) {
        this.clientes = clientes;
        this.ventas = ventas;
        this.sesion = sesion;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar(boolean soloActivos) {
        sesion.exigirUsuario();
        return soloActivos ? clientes.findByActivoTrueOrderByNombreAsc() : clientes.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Cliente consultar(Long id) {
        sesion.exigirUsuario();
        return buscar(id);
    }

    @Transactional(readOnly = true)
    public Cliente consultarDocumento(String documento) {
        sesion.exigirUsuario();
        return clientes.findByNumeroDocumento(Validacion.texto(documento, "Documento", 30))
                .orElseThrow(ClienteNoEncontradoException::new);
    }

    @Transactional
    public Cliente registrar(Cliente datos) {
        sesion.exigirUsuario();
        Cliente limpio = validar(datos, null);
        return clientes.save(limpio);
    }

    @Transactional
    public Cliente actualizar(Long id, Cliente datos) {
        sesion.exigirAdministrador();
        Cliente existente = buscar(id);
        existente.actualizar(validar(datos, id));
        return existente;
    }

    @Transactional
    public void eliminar(Long id) {
        sesion.exigirAdministrador();
        Cliente existente = buscar(id);
        if (ventas.existsByClienteId(id)) {
            existente.desactivar();
            clientes.save(existente);
            throw new DatosInvalidosException("No se puede borrar físicamente el cliente '" + existente.getNombre() + "' porque tiene ventas registradas en el historial. Ha sido desactivado.");
        }
        clientes.delete(existente);
    }

    @Transactional
    public void desactivar(Long id) {
        sesion.exigirAdministrador();
        buscar(id).desactivar();
    }

    private Cliente buscar(Long id) {
        Validacion.exigir(id != null, "Seleccione un cliente.");
        return clientes.findById(id).orElseThrow(ClienteNoEncontradoException::new);
    }

    private Cliente validar(Cliente datos, Long id) {
        Validacion.exigir(datos != null, "Ingrese los datos del cliente.");
        String documento = Validacion.texto(datos.getNumeroDocumento(), "Documento", 30);
        boolean duplicado = clientes.findByNumeroDocumento(documento)
                .filter(c -> !Objects.equals(c.getId(), id)).isPresent();
        Validacion.exigir(!duplicado, "Ya existe un cliente con ese documento.");
        return new Cliente(Validacion.texto(datos.getTipoDocumento(), "Tipo de documento", 20),
                documento, Validacion.texto(datos.getNombre(), "Nombre", 100),
                Validacion.opcional(datos.getApellido(), "Apellido", 100),
                Validacion.opcional(datos.getTelefono(), "Teléfono", 30), Validacion.correo(datos.getCorreo()));
    }
}
