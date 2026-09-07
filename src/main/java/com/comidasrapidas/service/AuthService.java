package com.comidasrapidas.service;

import com.comidasrapidas.exception.AccesoDenegadoException;
import com.comidasrapidas.model.Rol;
import com.comidasrapidas.model.Usuario;
import com.comidasrapidas.repository.UsuarioRepository;
import com.comidasrapidas.util.Validacion;
import jakarta.persistence.EntityManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository repositorioUsuarios;
    private final SesionService servicioSesion;
    private final BCryptPasswordEncoder codificador;
    private final EntityManager gestorEntidades;

    public AuthService(UsuarioRepository repositorioUsuarios, SesionService servicioSesion,
                       BCryptPasswordEncoder codificador, EntityManager gestorEntidades) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.servicioSesion = servicioSesion;
        this.codificador = codificador;
        this.gestorEntidades = gestorEntidades;
    }

    public boolean requiereConfiguracion() {
        return repositorioUsuarios.count() == 0;
    }

    @Transactional(readOnly = true)
    public Usuario iniciar(String nombreUsuario, String clave) {
        servicioSesion.cerrar();
        String nombreNormalizado = Validacion.username(nombreUsuario);
        Usuario usuario = repositorioUsuarios.findByUsername(nombreNormalizado).filter(Usuario::isActivo)
                .orElseThrow(this::credencialesInvalidas);
        if (clave == null || !codificador.matches(clave, usuario.getPassword())) {
            throw credencialesInvalidas();
        }
        servicioSesion.abrir(usuario);
        return usuario;
    }

    @Transactional
    public void configurarAdministrador(String nombre, String nombreUsuario, String clave) {
        // Serializa únicamente la primera configuración sin añadir una tabla.
        gestorEntidades.createNativeQuery("SELECT pg_advisory_xact_lock(72419031)").getSingleResult();
        Validacion.exigir(requiereConfiguracion(), "El administrador inicial ya fue configurado.");
        Validacion.password(clave);
        Usuario usuario = new Usuario(Validacion.texto(nombre, "Nombre", 100),
                Validacion.username(nombreUsuario), codificador.encode(clave), Rol.ADMINISTRADOR);
        repositorioUsuarios.saveAndFlush(usuario);
    }

    private AccesoDenegadoException credencialesInvalidas() {
        return new AccesoDenegadoException("Usuario o contraseña incorrectos, o cuenta inactiva.");
    }
}

