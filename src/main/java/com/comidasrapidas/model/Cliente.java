package com.comidasrapidas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 30)
    private String numeroDocumento;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, length = 30)
    private String telefono;

    @Column(nullable = false, length = 150)
    private String correo;

    @Column(nullable = false)
    private boolean activo;

    protected Cliente() {
    }

    public Cliente(String tipo, String documento, String nombre, String apellido,
                   String telefono, String correo) {
        this.tipoDocumento = tipo;
        this.numeroDocumento = documento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.correo = correo;
        this.activo = true;
    }

    public void actualizar(Cliente datos) {
        this.tipoDocumento = datos.tipoDocumento;
        this.numeroDocumento = datos.numeroDocumento;
        this.nombre = datos.nombre;
        this.apellido = datos.apellido;
        this.telefono = datos.telefono;
        this.correo = datos.correo;
    }

    public void desactivar() {
        this.activo = false;
    }

    @Override
    public String toString() {
        return nombre + " " + apellido + " · " + numeroDocumento;
    }

    public Long getId() {
        return id;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public boolean isActivo() {
        return activo;
    }

}
