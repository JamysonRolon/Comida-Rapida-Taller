package com.comidasrapidas.service;

import com.comidasrapidas.exception.*;
import com.comidasrapidas.model.*;
import com.comidasrapidas.repository.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {
    @Mock VentaRepository ventas;
    @Mock ProductoRepository productos;
    @Mock ClienteRepository clientes;
    @Mock SesionService sesion;
    private VentaService service;
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-06T15:00:00Z"), ZoneId.of("America/Bogota"));

    @BeforeEach void preparar() {
        service = new VentaService(ventas, productos, clientes, sesion, clock);
        lenient().when(sesion.exigirUsuario()).thenReturn(DatosPrueba.admin());
        lenient().when(ventas.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test void registraVentaSinClienteCalculaYDescuentaStock() {
        Producto producto = DatosPrueba.producto(10);
        when(productos.bloquear(1L)).thenReturn(Optional.of(producto));
        Venta venta = service.registrar(List.of(DatosPrueba.linea(1, 2, "12000.50")), null);
        assertNull(venta.getCliente());
        assertEquals(new BigDecimal("24001.00"), venta.getTotal());
        assertEquals(new BigDecimal("24001.00"), venta.getDetalles().getFirst().getSubtotal());
        assertEquals(8, producto.getStock());
        assertEquals("Hamburguesa (x2)", venta.getResumenProductos());
        assertEquals(clock.instant(), venta.getFechaHora());
    }

    @Test void sumaVariosSubtotalesConPrecisionDecimal() {
        Producto primero = DatosPrueba.producto(10);
        Producto segundo = DatosPrueba.id(new Producto("Bebida", "", new BigDecimal("0.10"), 10, DatosPrueba.categoria()), 2);
        when(productos.bloquear(1L)).thenReturn(Optional.of(primero));
        when(productos.bloquear(2L)).thenReturn(Optional.of(segundo));
        Venta venta = service.registrar(List.of(DatosPrueba.linea(2, 3, "0.10"), DatosPrueba.linea(1, 1, "12000.50")), null);
        assertEquals(new BigDecimal("12000.80"), venta.getTotal());
        InOrder orden = inOrder(productos);
        orden.verify(productos).bloquear(1L);
        orden.verify(productos).bloquear(2L);
    }

    @Test void permiteClienteRegistrado() {
        Cliente cliente = DatosPrueba.id(DatosPrueba.cliente(), 8);
        when(clientes.findById(8L)).thenReturn(Optional.of(cliente));
        when(productos.bloquear(1L)).thenReturn(Optional.of(DatosPrueba.producto(3)));
        Venta venta = service.registrar(List.of(DatosPrueba.linea(1, 1, "12000.50")), 8L);
        assertSame(cliente, venta.getCliente());
    }

    @Test void rechazaFaltaDeStock() {
        when(productos.bloquear(1L)).thenReturn(Optional.of(DatosPrueba.producto(1)));
        assertThrows(StockInsuficienteException.class,
                () -> service.registrar(List.of(DatosPrueba.linea(1, 2, "12000.50")), null));
        verify(ventas, never()).saveAndFlush(any());
    }

    @Test void rechazaCarritoVacio() {
        assertThrows(DatosInvalidosException.class, () -> service.registrar(List.of(), null));
    }

    @Test void rechazaCantidadCero() {
        assertThrows(DatosInvalidosException.class, () -> service.registrar(List.of(DatosPrueba.linea(1, 0, "1")), null));
    }

    @Test void rechazaProductoRepetido() {
        LineaCarrito linea = DatosPrueba.linea(1, 1, "1");
        assertThrows(DatosInvalidosException.class, () -> service.registrar(List.of(linea, linea), null));
    }

    @Test void rechazaPrecioCambiado() {
        when(productos.bloquear(1L)).thenReturn(Optional.of(DatosPrueba.producto(2)));
        assertThrows(DatosInvalidosException.class, () -> service.registrar(List.of(DatosPrueba.linea(1, 1, "12000")), null));
        verify(ventas, never()).saveAndFlush(any());
    }

    @Test void rechazaProductoInactivo() {
        Producto producto = DatosPrueba.producto(2);
        producto.desactivar();
        when(productos.bloquear(1L)).thenReturn(Optional.of(producto));
        assertThrows(DatosInvalidosException.class, () -> service.registrar(List.of(DatosPrueba.linea(1, 1, "12000.50")), null));
    }

    @Test void rechazaClienteInactivo() {
        Cliente cliente = DatosPrueba.cliente();
        cliente.desactivar();
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        assertThrows(DatosInvalidosException.class, () -> service.registrar(List.of(DatosPrueba.linea(1, 1, "1")), 1L));
    }

    @Test void consultaDiaConLimitesDeBogota() {
        service.consultarDia(LocalDate.of(2026, 9, 6));
        verify(ventas).findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraDesc(
                Instant.parse("2026-09-06T05:00:00Z"), Instant.parse("2026-09-07T05:00:00Z"));
    }

    @Test void totalDiaSinVentasEsCero() {
        when(ventas.sumarPeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, service.totalDia(LocalDate.of(2026, 9, 6)));
    }
}
