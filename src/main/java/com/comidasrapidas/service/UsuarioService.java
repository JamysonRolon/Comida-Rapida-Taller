package com.comidasrapidas.service;

import com.comidasrapidas.model.Rol;
import com.comidasrapidas.model.Usuario;
import com.comidasrapidas.repository.UsuarioRepository;
import com.comidasrapidas.util.Validacion;
import com.comidasrapidas.exception.DatosInvalidosException;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    private final UsuarioRepository repositorioUsuarios;
    private final SesionService servicioSesion;
    private final BCryptPasswordEncoder codificador;

    public UsuarioService(UsuarioRepository repositorioUsuarios, SesionService servicioSesion, BCryptPasswordEncoder codificador) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.servicioSesion = servicioSesion;
        this.codificador = codificador;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        servicioSesion.exigirAdministrador();
        return repositorioUsuarios.findAllByOrderByNombreAsc();
    }

    @Transactional
    public Usuario registrar(String nombre, String nombreUsuario, String clave, Rol rol) {
        servicioSesion.exigirAdministrador();
        String normalizado = Validacion.username(nombreUsuario);
        Validacion.exigir(repositorioUsuarios.findByUsername(normalizado).isEmpty(), "El nombre de usuario ya existe.");
        Validacion.password(clave);
        Validacion.exigir(rol != null, "Seleccione un rol.");
        return repositorioUsuarios.save(new Usuario(Validacion.texto(nombre, "Nombre", 100),
                normalizado, codificador.encode(clave), rol));
    }

    @Transactional
    public void cambiarPassword(Long id, String clave) {
        servicioSesion.exigirAdministrador();
        Validacion.password(clave);
        buscar(id).cambiarPassword(codificador.encode(clave));
    }

    @Transactional
    public void desactivar(Long id) {
        Usuario actual = servicioSesion.exigirAdministrador();
        Validacion.exigir(!actual.getId().equals(id), "No puede desactivar su propia cuenta.");
        buscar(id).desactivar();
    }

    private Usuario buscar(Long id) {
        Validacion.exigir(id != null, "Seleccione un empleado.");
        return repositorioUsuarios.findById(id).orElseThrow(() -> new DatosInvalidosException("El empleado no existe."));
    }
}

