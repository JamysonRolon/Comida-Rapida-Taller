package com.comidasrapidas.service;

import com.comidasrapidas.exception.ClienteNoEncontradoException;
import com.comidasrapidas.exception.DatosInvalidosException;
import com.comidasrapidas.exception.ProductoNoEncontradoException;
import com.comidasrapidas.model.*;
import com.comidasrapidas.repository.ClienteRepository;
import com.comidasrapidas.repository.ProductoRepository;
import com.comidasrapidas.repository.VentaRepository;
import com.comidasrapidas.util.Validacion;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VentaService {
    private final VentaRepository ventas;
    private final ProductoRepository productos;
    private final ClienteRepository clientes;
    private final SesionService sesion;
    private final Clock clock;

    public VentaService(VentaRepository ventas, ProductoRepository productos,
                        ClienteRepository clientes, SesionService sesion, Clock clock) {
        this.ventas = ventas;
        this.productos = productos;
        this.clientes = clientes;
        this.sesion = sesion;
        this.clock = clock;
    }

    @Transactional
    public Venta registrar(List<LineaCarrito> lineas, Long clienteId) {
        Usuario usuario = sesion.exigirUsuario();
        Map<Long, LineaCarrito> ordenadas = validarLineas(lineas);
        Venta venta = new Venta(clock.instant(), buscarCliente(clienteId), usuario);
        ordenadas.values().forEach(linea -> agregarDetalle(venta, linea));
        Validacion.precio(venta.getTotal());
        return ventas.saveAndFlush(venta);
    }

    @Transactional(readOnly = true)
    public List<Venta> consultarDia(LocalDate fecha) {
        sesion.exigirUsuario();
        Validacion.exigir(fecha != null, "Seleccione una fecha.");
        return ventas.findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraDesc(
                fecha.atStartOfDay(clock.getZone()).toInstant(),
                fecha.plusDays(1).atStartOfDay(clock.getZone()).toInstant());
    }

    @Transactional(readOnly = true)
    public List<Venta> consultarMes(LocalDate fecha) {
        sesion.exigirUsuario();
        Validacion.exigir(fecha != null, "Seleccione una fecha.");
        java.time.YearMonth mes = java.time.YearMonth.from(fecha);
        return ventas.findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraDesc(
                mes.atDay(1).atStartOfDay(clock.getZone()).toInstant(),
                mes.plusMonths(1).atDay(1).atStartOfDay(clock.getZone()).toInstant());
    }

    @Transactional(readOnly = true)
    public List<Venta> consultarTodas() {
        sesion.exigirUsuario();
        return ventas.findAllByOrderByFechaHoraDesc();
    }

    @Transactional(readOnly = true)
    public BigDecimal totalDia(LocalDate fecha) {
        sesion.exigirUsuario();
        Validacion.exigir(fecha != null, "Seleccione una fecha.");
        return ventas.sumarPeriodo(fecha.atStartOfDay(clock.getZone()).toInstant(),
                fecha.plusDays(1).atStartOfDay(clock.getZone()).toInstant());
    }


    @Transactional(readOnly = true)
    public Venta consultar(Long id) {
        sesion.exigirUsuario();
        Validacion.exigir(id != null, "Seleccione una venta.");
        return ventas.buscarCompleta(id).orElseThrow(() -> new DatosInvalidosException("La venta no existe."));
    }

    private Map<Long, LineaCarrito> validarLineas(List<LineaCarrito> lineas) {
        Validacion.exigir(lineas != null && !lineas.isEmpty(), "Agregue al menos un producto.");
        Map<Long, LineaCarrito> ordenadas = new TreeMap<>();
        for (LineaCarrito linea : lineas) {
            Validacion.exigir(linea != null && linea.getProductoId() != null, "La línea de venta no es válida.");
            Validacion.exigir(linea.getCantidad() > 0, "La cantidad debe ser mayor que cero.");
            Validacion.precio(linea.getPrecioUnitario());
            Validacion.exigir(ordenadas.put(linea.getProductoId(), linea) == null,
                    "El producto está repetido. Actualice el carrito.");
        }
        return ordenadas;
    }

    private Cliente buscarCliente(Long id) {
        if (id == null) {
            return null;
        }
        Cliente cliente = clientes.findById(id).orElseThrow(ClienteNoEncontradoException::new);
        Validacion.exigir(cliente.isActivo(), "El cliente está inactivo.");
        return cliente;
    }

    private void agregarDetalle(Venta venta, LineaCarrito linea) {
        Producto producto = productos.bloquear(linea.getProductoId())
                .orElseThrow(ProductoNoEncontradoException::new);
        Validacion.exigir(producto.isActivo(), "El producto " + producto.getNombre() + " está inactivo.");
        Validacion.exigir(producto.getPrecio().compareTo(linea.getPrecioUnitario()) == 0,
                "El precio de " + producto.getNombre() + " cambió. Quite la línea y agréguela de nuevo.");
        producto.descontar(linea.getCantidad());
        venta.agregar(producto, linea.getCantidad());
    }
}
