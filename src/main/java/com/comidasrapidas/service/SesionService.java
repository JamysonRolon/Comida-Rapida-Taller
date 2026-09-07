package com.comidasrapidas.service;

import com.comidasrapidas.exception.AccesoDenegadoException;
import com.comidasrapidas.model.Rol;
import com.comidasrapidas.model.Usuario;
import com.comidasrapidas.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class SesionService {
    private final UsuarioRepository usuarios;
    private volatile Long usuarioId;

    public SesionService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    void abrir(Usuario usuario) {
        usuarioId = usuario.getId();
    }

    public Usuario exigirUsuario() {
        Long id = usuarioId;
        if (id == null) {
            throw new AccesoDenegadoException("Debe iniciar sesión.");
        }
        return usuarios.findById(id).filter(Usuario::isActivo)
                .orElseThrow(() -> new AccesoDenegadoException("La sesión ya no está disponible. Inicie sesión de nuevo."));
    }

    public Usuario exigirAdministrador() {
        Usuario usuario = exigirUsuario();
        if (usuario.getRol() != Rol.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Esta operación requiere un administrador.");
        }
        return usuario;
    }

    public void cerrar() {
        usuarioId = null;
    }
}
