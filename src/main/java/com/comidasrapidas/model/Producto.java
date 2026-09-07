package com.comidasrapidas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import com.comidasrapidas.exception.StockInsuficienteException;
import com.comidasrapidas.exception.DatosInvalidosException;

@Entity
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private boolean activo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    protected Producto() {
    }

    public Producto(String nombre, String descripcion, BigDecimal precio, int stock, Categoria categoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.activo = true;
    }

    public void actualizar(Producto datos, Categoria categoria) {
        this.nombre = datos.nombre;
        this.descripcion = datos.descripcion;
        this.precio = datos.precio;
        this.stock = datos.stock;
        this.categoria = categoria;
    }

    public void descontar(int cantidad) {
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad debe ser mayor que cero.");
        }
        if (cantidad > stock) {
            throw new StockInsuficienteException();
        }
        stock -= cantidad;
    }

    public void desactivar() {
        this.activo = false;
    }

    @Override
    public String toString() {
        return nombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public int getStock() {
        return stock;
    }

    public boolean isActivo() {
        return activo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

}
