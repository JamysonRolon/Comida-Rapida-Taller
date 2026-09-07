package com.comidasrapidas.service;

import com.comidasrapidas.model.*;
import java.math.BigDecimal;
import org.springframework.test.util.ReflectionTestUtils;

final class DatosPrueba {
    private DatosPrueba() {
    }

    static <T> T id(T entidad, long id) {
        ReflectionTestUtils.setField(entidad, "id", id);
        return entidad;
    }

    static Cliente cliente() {
        return new Cliente("CC", "12345", "Ana", "Prueba", "", "ana@example.com");
    }

    static Categoria categoria() {
        return id(new Categoria("Hamburguesas"), 1);
    }

    static Producto producto(int stock) {
        return id(new Producto("Hamburguesa", "", new BigDecimal("12000.50"), stock, categoria()), 1);
    }

    static Usuario admin() {
        return id(new Usuario("Admin", "admin", "hash", Rol.ADMINISTRADOR), 1);
    }

    static LineaCarrito linea(long id, int cantidad, String precio) {
        return new LineaCarrito(id, "Producto", cantidad, new BigDecimal(precio));
    }
}
