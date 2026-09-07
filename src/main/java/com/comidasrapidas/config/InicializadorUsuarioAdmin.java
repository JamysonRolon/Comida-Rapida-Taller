package com.comidasrapidas.config;

import com.comidasrapidas.model.Rol;
import com.comidasrapidas.model.Usuario;
import com.comidasrapidas.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(10)
public class InicializadorUsuarioAdmin implements ApplicationRunner {
    private static final Logger REGISTRO = LoggerFactory.getLogger(InicializadorUsuarioAdmin.class);
    private static final String USUARIO_PEPITO = "pepito_admin";
    private static final String CLAVE_PEPITO = "PepitoAdmin123*";

    private final UsuarioRepository repositorioUsuarios;
    private final BCryptPasswordEncoder codificador;

    public InicializadorUsuarioAdmin(UsuarioRepository repositorioUsuarios, BCryptPasswordEncoder codificador) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.codificador = codificador;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments argumentos) {
        String claveEncriptada = codificador.encode(CLAVE_PEPITO);
        repositorioUsuarios.findByUsername(USUARIO_PEPITO).ifPresentOrElse(usuarioExistente -> {
            usuarioExistente.cambiarPassword(claveEncriptada);
            REGISTRO.info("Usuario administrador '{}' actualizado con contraseña funcional.", USUARIO_PEPITO);
        }, () -> {
            Usuario nuevoAdmin = new Usuario("Pepito Administrador", USUARIO_PEPITO, claveEncriptada, Rol.ADMINISTRADOR);
            repositorioUsuarios.save(nuevoAdmin);
            REGISTRO.info("Usuario administrador '{}' creado exitosamente.", USUARIO_PEPITO);
        });
    }
}
