package com.comidasrapidas.service;

import com.comidasrapidas.exception.DatosInvalidosException;
import com.comidasrapidas.model.Categoria;
import com.comidasrapidas.repository.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {
    @Mock CategoriaRepository categorias;
    @Mock ProductoRepository productos;
    @Mock SesionService sesion;
    @InjectMocks CategoriaService service;

    @Test void creaCategoriaConNombreLimpio() {
        when(categorias.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("Bebidas", service.registrar(" Bebidas ").getNombre());
    }

    @Test void rechazaNombreDuplicadoSinDistinguirMayusculas() {
        when(categorias.findByNombreIgnoreCase("bebidas")).thenReturn(Optional.of(DatosPrueba.categoria()));
        assertThrows(DatosInvalidosException.class, () -> service.registrar("bebidas"));
    }

    @Test void actualizaCategoria() {
        Categoria categoria = DatosPrueba.categoria();
        when(categorias.bloquear(1L)).thenReturn(Optional.of(categoria));
        assertEquals("Combos", service.actualizar(1L, "Combos").getNombre());
    }

    @Test void desactivaCategoriaSinProductosActivos() {
        Categoria categoria = DatosPrueba.categoria();
        when(categorias.bloquear(1L)).thenReturn(Optional.of(categoria));
        service.desactivar(1L);
        assertFalse(categoria.isActivo());
    }

    @Test void rechazaDesactivarCategoriaConProductosActivos() {
        when(categorias.bloquear(1L)).thenReturn(Optional.of(DatosPrueba.categoria()));
        when(productos.existsByCategoriaIdAndActivoTrue(1L)).thenReturn(true);
        assertThrows(DatosInvalidosException.class, () -> service.desactivar(1L));
    }
}
