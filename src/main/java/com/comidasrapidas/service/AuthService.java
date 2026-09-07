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
    private final UsuarioRepository usuarios;
    private final SesionService sesion;
    private final BCryptPasswordEncoder encoder;
    private final EntityManager entityManager;

    public AuthService(UsuarioRepository usuarios, SesionService sesion,
                       BCryptPasswordEncoder encoder, EntityManager entityManager) {
        this.usuarios = usuarios;
        this.sesion = sesion;
        this.encoder = encoder;
        this.entityManager = entityManager;
    }

    public boolean requiereConfiguracion() {
        return usuarios.count() == 0;
    }

    @Transactional(readOnly = true)
    public Usuario iniciar(String username, String password) {
        sesion.cerrar();
        String nombre = Validacion.username(username);
        Usuario usuario = usuarios.findByUsername(nombre).filter(Usuario::isActivo)
                .orElseThrow(this::credencialesInvalidas);
        if (password == null || !encoder.matches(password, usuario.getPassword())) {
            throw credencialesInvalidas();
        }
        sesion.abrir(usuario);
        return usuario;
    }

    @Transactional
    public void configurarAdministrador(String nombre, String username, String password) {
        // Serializa únicamente la primera configuración sin añadir una tabla.
        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(72419031)").getSingleResult();
        Validacion.exigir(requiereConfiguracion(), "El administrador inicial ya fue configurado.");
        Validacion.password(password);
        Usuario usuario = new Usuario(Validacion.texto(nombre, "Nombre", 100),
                Validacion.username(username), encoder.encode(password), Rol.ADMINISTRADOR);
        usuarios.saveAndFlush(usuario);
    }

    private AccesoDenegadoException credencialesInvalidas() {
        return new AccesoDenegadoException("Usuario o contraseña incorrectos, o cuenta inactiva.");
    }
}
