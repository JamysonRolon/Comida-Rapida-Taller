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
    private final UsuarioRepository usuarios;
    private final SesionService sesion;
    private final BCryptPasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, SesionService sesion, BCryptPasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.sesion = sesion;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        sesion.exigirAdministrador();
        return usuarios.findAllByOrderByNombreAsc();
    }

    @Transactional
    public Usuario registrar(String nombre, String username, String password, Rol rol) {
        sesion.exigirAdministrador();
        String normalizado = Validacion.username(username);
        Validacion.exigir(usuarios.findByUsername(normalizado).isEmpty(), "El nombre de usuario ya existe.");
        Validacion.password(password);
        Validacion.exigir(rol != null, "Seleccione un rol.");
        return usuarios.save(new Usuario(Validacion.texto(nombre, "Nombre", 100),
                normalizado, encoder.encode(password), rol));
    }

    @Transactional
    public void cambiarPassword(Long id, String password) {
        sesion.exigirAdministrador();
        Validacion.password(password);
        buscar(id).cambiarPassword(encoder.encode(password));
    }

    @Transactional
    public void desactivar(Long id) {
        Usuario actual = sesion.exigirAdministrador();
        Validacion.exigir(!actual.getId().equals(id), "No puede desactivar su propia cuenta.");
        buscar(id).desactivar();
    }

    private Usuario buscar(Long id) {
        Validacion.exigir(id != null, "Seleccione un empleado.");
        return usuarios.findById(id).orElseThrow(() -> new DatosInvalidosException("El empleado no existe."));
    }
}
