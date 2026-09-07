package com.comidasrapidas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false)
    private boolean activo;

    protected Categoria() {
    }

    public Categoria(String nombre) {
        this.nombre = nombre;
        this.activo = true;
    }

    public void renombrar(String nombre) {
        this.nombre = nombre;
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

    public boolean isActivo() {
        return activo;
    }

}
