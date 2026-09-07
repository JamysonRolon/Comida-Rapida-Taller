package com.comidasrapidas.model;

import java.math.BigDecimal;

public final class LineaCarrito {
    private final Long productoId;
    private final String nombre;
    private final int cantidad;
    private final BigDecimal precioUnitario;

    public LineaCarrito(Long productoId, String nombre, int cantidad, BigDecimal precioUnitario) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Long getProductoId() {
        return productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
