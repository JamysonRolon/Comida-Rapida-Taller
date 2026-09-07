package com.comidasrapidas.service;

import com.comidasrapidas.exception.*;
import com.comidasrapidas.model.Cliente;
import com.comidasrapidas.repository.ClienteRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {
    @Mock ClienteRepository repository;
    @Mock com.comidasrapidas.repository.VentaRepository ventas;
    @Mock SesionService sesion;
    @InjectMocks ClienteService service;

    @Test void registraCliente() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        Cliente resultado = service.registrar(DatosPrueba.cliente());
        assertEquals("12345", resultado.getNumeroDocumento());
        assertTrue(resultado.isActivo());
        verify(sesion).exigirUsuario();
    }

    @Test void rechazaDocumentoDuplicadoInclusoInactivo() {
        Cliente existente = DatosPrueba.id(DatosPrueba.cliente(), 2);
        existente.desactivar();
        when(repository.findByNumeroDocumento("12345")).thenReturn(Optional.of(existente));
        assertThrows(DatosInvalidosException.class, () -> service.registrar(DatosPrueba.cliente()));
        verify(repository, never()).save(any());
    }

    @Test void consultaCliente() {
        Cliente cliente = DatosPrueba.id(DatosPrueba.cliente(), 1);
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));
        assertSame(cliente, service.consultar(1L));
    }

    @Test void consultaPorDocumento() {
        Cliente cliente = DatosPrueba.cliente();
        when(repository.findByNumeroDocumento("12345")).thenReturn(Optional.of(cliente));
        assertSame(cliente, service.consultarDocumento(" 12345 "));
    }

    @Test void informaClienteInexistente() {
        assertThrows(ClienteNoEncontradoException.class, () -> service.consultar(99L));
    }

    @Test void actualizaClienteSinDuplicarse() {
        Cliente original = DatosPrueba.id(DatosPrueba.cliente(), 1);
        when(repository.findById(1L)).thenReturn(Optional.of(original));
        when(repository.findByNumeroDocumento("12345")).thenReturn(Optional.of(original));
        Cliente cambios = new Cliente("CC", "12345", "Nuevo", "", "", "");
        assertEquals("Nuevo", service.actualizar(1L, cambios).getNombre());
        verify(sesion).exigirAdministrador();
    }

    @Test void desactivaSinBorrar() {
        Cliente cliente = DatosPrueba.id(DatosPrueba.cliente(), 1);
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));
        service.desactivar(1L);
        assertFalse(cliente.isActivo());
        verify(repository, never()).delete(any());
    }

    @Test void rechazaCorreoInvalido() {
        Cliente datos = new Cliente("CC", "12", "Ana", "", "", "correo-invalido");
        assertThrows(DatosInvalidosException.class, () -> service.registrar(datos));
    }

    @Test void rechazaNombreVacio() {
        Cliente datos = new Cliente("CC", "12", " ", "", "", "");
        assertThrows(DatosInvalidosException.class, () -> service.registrar(datos));
    }

    @Test void cajeroNoPuedeActualizar() {
        doThrow(new AccesoDenegadoException("Sin permiso")).when(sesion).exigirAdministrador();
        assertThrows(AccesoDenegadoException.class, () -> service.actualizar(1L, DatosPrueba.cliente()));
        verifyNoInteractions(repository);
    }

    @Test void eliminaFisicamenteClienteSinVentas() {
        Cliente cliente = DatosPrueba.id(DatosPrueba.cliente(), 1);
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));
        when(ventas.existsByClienteId(1L)).thenReturn(false);
        service.eliminar(1L);
        verify(repository).delete(cliente);
    }

    @Test void desactivaClienteConVentasAlEliminar() {
        Cliente cliente = DatosPrueba.id(DatosPrueba.cliente(), 1);
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));
        when(ventas.existsByClienteId(1L)).thenReturn(true);
        assertThrows(DatosInvalidosException.class, () -> service.eliminar(1L));
        assertFalse(cliente.isActivo());
        verify(repository).save(cliente);
        verify(repository, never()).delete(any());
    }
}
