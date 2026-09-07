package com.comidasrapidas.service;

import com.comidasrapidas.model.*;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComprobanteServiceTest {
    @Test void usaPrecioHistoricoTrasEditarProducto() {
        Producto producto = DatosPrueba.producto(5);
        Venta venta = DatosPrueba.id(new Venta(Instant.parse("2026-09-06T15:00:00Z"), null, DatosPrueba.admin()), 42);
        venta.agregar(producto, 2);
        producto.actualizar(new Producto("Hamburguesa", "", new BigDecimal("99000"), 3, DatosPrueba.categoria()), DatosPrueba.categoria());
        String texto = new ComprobanteService().generar(venta);
        assertTrue(texto.contains("Venta n.º 42"));
        assertTrue(texto.contains("06/09/2026 10:00:00"));
        assertTrue(texto.contains("consumidor sin registro"));
        assertTrue(texto.contains("efectivo"));
        assertEquals(new BigDecimal("12000.50"), venta.getDetalles().getFirst().getPrecioUnitario());
        assertEquals(new BigDecimal("24001.00"), venta.getTotal());
    }
}
