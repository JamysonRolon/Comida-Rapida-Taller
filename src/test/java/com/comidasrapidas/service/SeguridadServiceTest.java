package com.comidasrapidas.service;

import com.comidasrapidas.exception.*;
import com.comidasrapidas.model.*;
import com.comidasrapidas.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SeguridadServiceTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final SesionService sesion = new SesionService(usuarios);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final AuthService auth = new AuthService(usuarios, sesion, encoder, mock(EntityManager.class));

    @Test void autenticaConHashYUsernameNormalizado() {
        Usuario usuario = DatosPrueba.id(new Usuario("Ana", "ana", encoder.encode("ClavePrueba123"), Rol.CAJERO), 1);
        when(usuarios.findByUsername("ana")).thenReturn(Optional.of(usuario));
        when(usuarios.findById(1L)).thenReturn(Optional.of(usuario));
        assertSame(usuario, auth.iniciar(" ANA ", "ClavePrueba123"));
        assertSame(usuario, sesion.exigirUsuario());
        assertNotEquals("ClavePrueba123", usuario.getPassword());
    }

    @Test void rechazaPasswordIncorrecta() {
        Usuario usuario = DatosPrueba.id(new Usuario("Ana", "ana", encoder.encode("ClavePrueba123"), Rol.CAJERO), 1);
        when(usuarios.findByUsername("ana")).thenReturn(Optional.of(usuario));
        assertThrows(AccesoDenegadoException.class, () -> auth.iniciar("ana", "incorrecta"));
        assertThrows(AccesoDenegadoException.class, sesion::exigirUsuario);
    }

    @Test void rechazaUsuarioInactivo() {
        Usuario usuario = DatosPrueba.admin();
        usuario.desactivar();
        when(usuarios.findByUsername("admin")).thenReturn(Optional.of(usuario));
        assertThrows(AccesoDenegadoException.class, () -> auth.iniciar("admin", "cualquierClave"));
    }

    @Test void rechazaSinSesion() {
        assertThrows(AccesoDenegadoException.class, sesion::exigirUsuario);
    }

    @Test void cajeroNoTienePermisoAdministrador() {
        Usuario cajero = DatosPrueba.id(new Usuario("Ana", "ana", "hash", Rol.CAJERO), 1);
        when(usuarios.findById(1L)).thenReturn(Optional.of(cajero));
        sesion.abrir(cajero);
        assertThrows(AccesoDenegadoException.class, sesion::exigirAdministrador);
    }

    @Test void desactivacionInvalidaSesionAbierta() {
        Usuario admin = DatosPrueba.admin();
        when(usuarios.findById(1L)).thenReturn(Optional.of(admin));
        sesion.abrir(admin);
        admin.desactivar();
        assertThrows(AccesoDenegadoException.class, sesion::exigirUsuario);
    }

    @Test void cerrarDescartaSesion() {
        sesion.abrir(DatosPrueba.admin());
        sesion.cerrar();
        assertThrows(AccesoDenegadoException.class, sesion::exigirUsuario);
    }

    @Test void administradorNoPuedeDesactivarse() {
        Usuario admin = DatosPrueba.admin();
        when(usuarios.findById(1L)).thenReturn(Optional.of(admin));
        sesion.abrir(admin);
        UsuarioService servicio = new UsuarioService(usuarios, sesion, encoder);
        assertThrows(DatosInvalidosException.class, () -> servicio.desactivar(1L));
        assertTrue(admin.isActivo());
    }

    @Test void contrasenaCortaSeRechaza() {
        assertThrows(DatosInvalidosException.class, () -> com.comidasrapidas.util.Validacion.password("corta"));
    }

    @Test void hashTieneSalDistintaPorCuenta() {
        assertNotEquals(encoder.encode("ClavePrueba123"), encoder.encode("ClavePrueba123"));
    }
}
