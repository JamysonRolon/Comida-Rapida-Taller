package com.comidasrapidas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "venta")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    protected Venta() {
    }

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<DetalleVenta> detalles = new ArrayList<>();

    public Venta(Instant fechaHora, Cliente cliente, Usuario usuario) {
        this.fechaHora = fechaHora;
        this.cliente = cliente;
        this.usuario = usuario;
        this.total = BigDecimal.ZERO.setScale(2);
    }

    public void agregar(Producto producto, int cantidad) {
        DetalleVenta detalle = new DetalleVenta(this, producto, cantidad);
        detalles.add(detalle);
        total = total.add(detalle.getSubtotal());
    }

    public List<DetalleVenta> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public Long getId() {
        return id;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

}
