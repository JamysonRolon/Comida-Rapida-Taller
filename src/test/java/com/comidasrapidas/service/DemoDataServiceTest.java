package com.comidasrapidas.service;

import com.comidasrapidas.model.Categoria;
import com.comidasrapidas.model.Cliente;
import com.comidasrapidas.model.Producto;
import com.comidasrapidas.model.Rol;
import com.comidasrapidas.model.Usuario;
import com.comidasrapidas.model.Venta;
import com.comidasrapidas.repository.CategoriaRepository;
import com.comidasrapidas.repository.ClienteRepository;
import com.comidasrapidas.repository.ProductoRepository;
import com.comidasrapidas.repository.UsuarioRepository;
import com.comidasrapidas.repository.VentaRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemoDataServiceTest {
    private static final String CLAVE_DANIEL_PRUEBA = "ClaveDanielPrueba";
    private static final String CLAVE_JUAN_PRUEBA = "ClaveJuanPrueba";
    @Mock UsuarioRepository usuarios;
    @Mock CategoriaRepository categorias;
    @Mock ProductoRepository productos;
    @Mock ClienteRepository clientes;
    @Mock VentaRepository ventas;
    private DemoDataService servicio;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void preparar() {
        encoder = new BCryptPasswordEncoder(4);
        Clock clock = Clock.fixed(Instant.parse("2026-09-06T15:00:00Z"), ZoneId.of("America/Bogota"));
        servicio = new DemoDataService(usuarios, categorias, productos, clientes, ventas, encoder, clock);
    }

    @Test
    void creaCatalogoClientesEmpleadosYVentasFicticias() {
        when(usuarios.findByUsername(any())).thenReturn(Optional.empty());
        when(categorias.findByNombreIgnoreCase(any())).thenReturn(Optional.empty());
        when(productos.findByNombreIgnoreCase(any())).thenReturn(Optional.empty());
        when(clientes.findByNumeroDocumento(any())).thenReturn(Optional.empty());
        when(usuarios.save(any())).thenAnswer(i -> i.getArgument(0));
        when(categorias.save(any())).thenAnswer(i -> i.getArgument(0));
        when(productos.save(any())).thenAnswer(i -> i.getArgument(0));
        when(clientes.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ventas.count()).thenReturn(0L);

        DemoDataSummary resumen = servicio.cargar(CLAVE_DANIEL_PRUEBA, CLAVE_JUAN_PRUEBA);

        assertEquals(new DemoDataSummary(2, 5, 12, 5, 3), resumen);
        verificarEmpleados();
        verificarCatalogoYStock();
        verify(categorias, times(5)).save(any(Categoria.class));
        verify(clientes, times(5)).save(any(Cliente.class));
        verify(ventas, times(3)).save(any(Venta.class));
    }

    @Test
    void noDuplicaRegistrosNiVentasExistentes() {
        Usuario daniel = new Usuario("Daniel", "daniel", "hash", Rol.CAJERO);
        Usuario juan = new Usuario("Juan", "juan", "hash", Rol.ADMINISTRADOR);
        when(usuarios.findByUsername("daniel")).thenReturn(Optional.of(daniel));
        when(usuarios.findByUsername("juan")).thenReturn(Optional.of(juan));
        when(categorias.findByNombreIgnoreCase(any())).thenAnswer(i ->
                Optional.of(new Categoria(i.getArgument(0))));
        Categoria categoria = new Categoria("Existente");
        when(productos.findByNombreIgnoreCase(any())).thenAnswer(i -> Optional.of(
                new Producto(i.getArgument(0), "", BigDecimal.ONE, 10, categoria)));
        when(clientes.findByNumeroDocumento(any())).thenAnswer(i -> Optional.of(
                new Cliente("CC", i.getArgument(0), "Existente", "", "", "")));
        when(ventas.count()).thenReturn(3L);

        DemoDataSummary resumen = servicio.cargar(CLAVE_DANIEL_PRUEBA, CLAVE_JUAN_PRUEBA);

        assertEquals(new DemoDataSummary(2, 5, 12, 5, 0), resumen);
        verify(usuarios, never()).save(any());
        verify(categorias, never()).save(any());
        verify(productos, never()).save(any());
        verify(clientes, never()).save(any());
        verify(ventas, never()).save(any());
    }

    private void verificarEmpleados() {
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios, times(2)).save(captor.capture());
        List<Usuario> guardados = captor.getAllValues();
        Usuario daniel = guardados.stream().filter(u -> u.getUsername().equals("daniel")).findFirst().orElseThrow();
        Usuario juan = guardados.stream().filter(u -> u.getUsername().equals("juan")).findFirst().orElseThrow();
        assertEquals("Daniel", daniel.getNombre());
        assertEquals(Rol.CAJERO, daniel.getRol());
        assertTrue(encoder.matches(CLAVE_DANIEL_PRUEBA, daniel.getPassword()));
        assertEquals("Juan", juan.getNombre());
        assertEquals(Rol.ADMINISTRADOR, juan.getRol());
        assertTrue(encoder.matches(CLAVE_JUAN_PRUEBA, juan.getPassword()));
        assertNotEquals(CLAVE_DANIEL_PRUEBA, daniel.getPassword());
    }

    private void verificarCatalogoYStock() {
        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productos, times(12)).save(captor.capture());
        List<Producto> guardados = captor.getAllValues();
        Producto hamburguesa = buscar(guardados, "Hamburguesa clásica");
        Producto gaseosa = buscar(guardados, "Gaseosa 400 ml");
        Producto comboFamiliar = buscar(guardados, "Combo familiar");
        assertEquals(28, hamburguesa.getStock());
        assertEquals(58, gaseosa.getStock());
        assertEquals(9, comboFamiliar.getStock());
        assertEquals(new BigDecimal("15000"), hamburguesa.getPrecio());
    }

    private Producto buscar(List<Producto> productos, String nombre) {
        return productos.stream().filter(p -> p.getNombre().equals(nombre)).findFirst().orElseThrow();
    }
}
