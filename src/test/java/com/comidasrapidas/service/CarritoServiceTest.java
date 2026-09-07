package com.comidasrapidas.service;

import com.comidasrapidas.exception.*;
import com.comidasrapidas.model.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CarritoServiceTest {
    private final CarritoService carrito = new CarritoService();

    @Test void acumulaCantidadesDeUnProducto() {
        carrito.agregar(DatosPrueba.producto(8), 2);
        carrito.agregar(DatosPrueba.producto(8), 3);
        assertEquals(1, carrito.getLineas().size());
        assertEquals(5, carrito.getLineas().getFirst().getCantidad());
        assertEquals(new BigDecimal("60002.50"), carrito.total());
    }

    @Test void rechazaAcumuladoMayorAlStock() {
        carrito.agregar(DatosPrueba.producto(3), 2);
        assertThrows(StockInsuficienteException.class, () -> carrito.agregar(DatosPrueba.producto(3), 2));
        assertEquals(2, carrito.getLineas().getFirst().getCantidad());
    }

    @Test void rechazaCantidadNegativa() {
        assertThrows(DatosInvalidosException.class, () -> carrito.agregar(DatosPrueba.producto(3), -1));
    }

    @Test void quitaYVacia() {
        carrito.agregar(DatosPrueba.producto(3), 2);
        carrito.quitar(1L);
        assertTrue(carrito.getLineas().isEmpty());
        carrito.agregar(DatosPrueba.producto(3), 1);
        carrito.vaciar();
        assertEquals(new BigDecimal("0.00"), carrito.total());
    }

    @Test void conservaSnapshotDelPrecioCotizado() {
        Producto producto = DatosPrueba.producto(3);
        carrito.agregar(producto, 1);
        producto.actualizar(new Producto("Nuevo", "", new BigDecimal("15000"), 3, DatosPrueba.categoria()), DatosPrueba.categoria());
        assertEquals(new BigDecimal("12000.50"), carrito.getLineas().getFirst().getPrecioUnitario());
    }
}
