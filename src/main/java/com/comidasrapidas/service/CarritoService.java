package com.comidasrapidas.service;

import com.comidasrapidas.exception.StockInsuficienteException;
import com.comidasrapidas.model.LineaCarrito;
import com.comidasrapidas.model.Producto;
import com.comidasrapidas.util.Validacion;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CarritoService {
    private final Map<Long, LineaCarrito> lineas = new LinkedHashMap<>();

    public void agregar(Producto producto, int cantidad) {
        Validacion.exigir(producto != null, "Seleccione un producto.");
        Validacion.exigir(producto.getId() != null && producto.isActivo(), "El producto no está disponible.");
        Validacion.exigir(cantidad > 0, "La cantidad debe ser mayor que cero.");
        LineaCarrito anterior = lineas.get(producto.getId());
        long acumulada = cantidad + (anterior == null ? 0L : anterior.getCantidad());
        if (acumulada > producto.getStock()) {
            throw new StockInsuficienteException();
        }
        lineas.put(producto.getId(), new LineaCarrito(producto.getId(), producto.getNombre(),
                (int) acumulada, Validacion.precio(producto.getPrecio())));
    }

    public void quitar(Long productoId) {
        lineas.remove(productoId);
    }

    public List<LineaCarrito> getLineas() {
        return List.copyOf(lineas.values());
    }

    public BigDecimal total() {
        return lineas.values().stream().map(LineaCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    public void vaciar() {
        lineas.clear();
    }
}
