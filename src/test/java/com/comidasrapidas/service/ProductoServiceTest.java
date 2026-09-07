package com.comidasrapidas.service;

import com.comidasrapidas.exception.*;
import com.comidasrapidas.model.*;
import com.comidasrapidas.repository.*;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {
    @Mock ProductoRepository productos;
    @Mock CategoriaRepository categorias;
    @Mock SesionService sesion;
    @InjectMocks ProductoService service;

    @Test void registraProducto() {
        when(categorias.bloquear(1L)).thenReturn(Optional.of(DatosPrueba.categoria()));
        when(productos.save(any())).thenAnswer(i -> i.getArgument(0));
        Producto resultado = service.registrar(DatosPrueba.producto(5));
        assertEquals(5, resultado.getStock());
        assertEquals(new BigDecimal("12000.50"), resultado.getPrecio());
    }

    @Test void consultaProducto() {
        Producto producto = DatosPrueba.producto(5);
        when(productos.findById(1L)).thenReturn(Optional.of(producto));
        assertSame(producto, service.consultar(1L));
    }

    @Test void actualizaProducto() {
        Producto original = DatosPrueba.producto(5);
        when(productos.bloquear(1L)).thenReturn(Optional.of(original));
        when(categorias.bloquear(1L)).thenReturn(Optional.of(DatosPrueba.categoria()));
        Producto cambios = new Producto("Combo", "Con bebida", new BigDecimal("18000"), 8, DatosPrueba.categoria());
        Producto resultado = service.actualizar(1L, cambios, 5);
        assertEquals("Combo", resultado.getNombre());
        assertEquals(8, resultado.getStock());
    }

    @Test void rechazaEdicionConStockDesactualizado() {
        when(productos.bloquear(1L)).thenReturn(Optional.of(DatosPrueba.producto(4)));
        assertThrows(DatosInvalidosException.class, () -> service.actualizar(1L, DatosPrueba.producto(9), 5));
    }

    @Test void desactivaProducto() {
        Producto producto = DatosPrueba.producto(5);
        when(productos.bloquear(1L)).thenReturn(Optional.of(producto));
        service.desactivar(1L);
        assertFalse(producto.isActivo());
        verify(productos, never()).delete(any());
    }

    @Test void rechazaPrecioCero() {
        when(categorias.bloquear(1L)).thenReturn(Optional.of(DatosPrueba.categoria()));
        Producto producto = new Producto("X", "", BigDecimal.ZERO, 1, DatosPrueba.categoria());
        assertThrows(DatosInvalidosException.class, () -> service.registrar(producto));
    }

    @Test void rechazaPrecioConTresDecimales() {
        when(categorias.bloquear(1L)).thenReturn(Optional.of(DatosPrueba.categoria()));
        Producto producto = new Producto("X", "", new BigDecimal("1.123"), 1, DatosPrueba.categoria());
        assertThrows(DatosInvalidosException.class, () -> service.registrar(producto));
    }

    @Test void rechazaStockNegativo() {
        assertThrows(DatosInvalidosException.class, () -> service.registrar(DatosPrueba.producto(-1)));
        verify(productos, never()).save(any());
    }

    @Test void rechazaCategoriaInactiva() {
        Categoria categoria = DatosPrueba.categoria();
        categoria.desactivar();
        when(categorias.bloquear(1L)).thenReturn(Optional.of(categoria));
        assertThrows(DatosInvalidosException.class, () -> service.registrar(DatosPrueba.producto(1)));
    }

    @Test void informaProductoInexistente() {
        assertThrows(ProductoNoEncontradoException.class, () -> service.consultar(99L));
    }
}
