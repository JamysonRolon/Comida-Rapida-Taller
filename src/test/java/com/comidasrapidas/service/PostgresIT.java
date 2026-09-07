package com.comidasrapidas.service;

import com.comidasrapidas.config.AppConfig;
import com.comidasrapidas.exception.*;
import com.comidasrapidas.model.*;
import com.comidasrapidas.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AppConfig.class, properties = "logging.level.org.hibernate.SQL=WARN")
@EnabledIfEnvironmentVariable(named = "RUN_PG_IT", matches = "true")
class PostgresIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired AuthService auth;
    @Autowired SesionService sesion;
    @Autowired CategoriaService categorias;
    @Autowired ClienteService clientes;
    @Autowired ProductoService productos;
    @Autowired VentaService ventas;
    @Autowired UsuarioService usuarios;
    @Autowired ProductoRepository productoRepository;
    @Autowired VentaRepository ventaRepository;
    @Autowired DetalleVentaRepository detalles;
    @Autowired UsuarioRepository usuarioRepository;
    private Categoria categoria;

    @DynamicPropertySource
    static void configurar(DynamicPropertyRegistry propiedades) {
        String url = System.getenv("TEST_DB_URL");
        if (url == null || !url.endsWith("/comidas_pruebas")) {
            throw new IllegalArgumentException("Las pruebas requieren una base exclusiva llamada comidas_pruebas.");
        }
        propiedades.add("spring.datasource.url", () -> url);
        propiedades.add("spring.datasource.username", () -> System.getenv("TEST_DB_USER"));
        propiedades.add("spring.datasource.password", () -> System.getenv("TEST_DB_PASSWORD"));
    }

    @BeforeEach
    void preparar() {
        sesion.cerrar();
        jdbc.execute("TRUNCATE detalle_venta, venta, producto, categoria, cliente, usuario RESTART IDENTITY CASCADE");
        auth.configurarAdministrador("Administrador de prueba", "admin", "ClaveSoloPruebas123");
        auth.iniciar("admin", "ClaveSoloPruebas123");
        categoria = categorias.registrar("Pruebas");
    }

    private Producto producto(String nombre, int stock) {
        return productos.registrar(new Producto(nombre, "", new BigDecimal("10000.25"), stock, categoria));
    }

    private LineaCarrito linea(Producto producto, int cantidad) {
        return new LineaCarrito(producto.getId(), producto.getNombre(), cantidad, producto.getPrecio());
    }

    @Test void persisteVentaCompletaConStockYComprobanteRecuperable() {
        Producto producto = producto("Hamburguesa", 5);
        Cliente cliente = clientes.registrar(DatosPrueba.cliente());
        Venta resultado = ventas.registrar(List.of(linea(producto, 2)), cliente.getId());
        Venta recuperada = ventas.consultar(resultado.getId());
        assertEquals(new BigDecimal("20000.50"), recuperada.getTotal());
        assertEquals(1, recuperada.getDetalles().size());
        assertEquals(3, productos.consultar(producto.getId()).getStock());
        assertEquals(cliente.getId(), recuperada.getCliente().getId());
        assertTrue(new ComprobanteService().generar(recuperada).contains("Ana"));
    }

    @Test void revierteTodoSiSegundoProductoNoTieneStock() {
        Producto primero = producto("Primero", 5);
        Producto segundo = producto("Segundo", 0);
        assertThrows(StockInsuficienteException.class,
                () -> ventas.registrar(List.of(linea(primero, 2), linea(segundo, 1)), null));
        assertEquals(5, productos.consultar(primero.getId()).getStock());
        assertEquals(0, ventaRepository.count());
        assertEquals(0, detalles.count());
    }

    @Test void dosVentasSimultaneasNoSobrevenden() throws Exception {
        Producto producto = producto("Última unidad", 1);
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> operacion = () -> {
                inicio.await();
                try {
                    ventas.registrar(List.of(linea(producto, 1)), null);
                    return true;
                } catch (StockInsuficienteException esperado) {
                    return false;
                }
            };
            Future<Boolean> primera = pool.submit(operacion);
            Future<Boolean> segunda = pool.submit(operacion);
            inicio.countDown();
            assertNotEquals(primera.get(15, TimeUnit.SECONDS), segunda.get(15, TimeUnit.SECONDS));
            assertEquals(0, productos.consultar(producto.getId()).getStock());
            assertEquals(1, ventaRepository.count());
            assertEquals(1, detalles.count());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test void restriccionesSqlRechazanStockNegativoYDuplicados() {
        Producto producto = producto("Prueba", 1);
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE producto SET stock = -1 WHERE id = ?", producto.getId()));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> jdbc.update("INSERT INTO categoria(nombre, activo) VALUES ('PRUEBAS', true)"));
        assertEquals(1, productos.consultar(producto.getId()).getStock());
    }

    @Test void crudPersistidoConservaHistorialAlDesactivar() {
        Cliente cliente = clientes.registrar(DatosPrueba.cliente());
        cliente = clientes.actualizar(cliente.getId(), new Cliente("CC", "12345", "Ana María", "", "", ""));
        Producto producto = producto("Prueba", 5);
        Venta venta = ventas.registrar(List.of(linea(producto, 1)), cliente.getId());
        clientes.desactivar(cliente.getId());
        productos.desactivar(producto.getId());
        assertFalse(clientes.consultar(cliente.getId()).isActivo());
        assertFalse(productos.consultar(producto.getId()).isActivo());
        assertEquals(new BigDecimal("10000.25"), ventas.consultar(venta.getId()).getTotal());
    }

    @Test void precioHistoricoNoCambiaTrasEditarCatalogo() {
        Producto producto = producto("Prueba", 5);
        Venta venta = ventas.registrar(List.of(linea(producto, 2)), null);
        productos.actualizar(producto.getId(), new Producto("Prueba", "", new BigDecimal("99000"), 3, categoria), 3);
        Venta recuperada = ventas.consultar(venta.getId());
        assertEquals(new BigDecimal("10000.25"), recuperada.getDetalles().getFirst().getPrecioUnitario());
        assertEquals(new BigDecimal("20000.50"), recuperada.getTotal());
    }

    @Test void limitesDelDiaSeConsultanEnBogota() {
        Producto producto = producto("Prueba", 10);
        Venta anterior = ventas.registrar(List.of(linea(producto, 1)), null);
        Venta inicio = ventas.registrar(List.of(linea(producto, 1)), null);
        Venta siguiente = ventas.registrar(List.of(linea(producto, 1)), null);
        jdbc.update("UPDATE venta SET fecha_hora = '2026-09-06T04:59:59Z' WHERE id = ?", anterior.getId());
        jdbc.update("UPDATE venta SET fecha_hora = '2026-09-06T05:00:00Z' WHERE id = ?", inicio.getId());
        jdbc.update("UPDATE venta SET fecha_hora = '2026-09-07T05:00:00Z' WHERE id = ?", siguiente.getId());
        LocalDate dia = LocalDate.of(2026, 9, 6);
        assertEquals(1, ventas.consultarDia(dia).size());
        assertEquals(new BigDecimal("10000.25"), ventas.totalDia(dia));
    }

    @Test void empleadosUsanHashYPermisosReales() {
        Usuario cajero = usuarios.registrar("Cajero de prueba", "cajero", "ClaveCajero123", Rol.CAJERO);
        assertNotEquals("ClaveCajero123", usuarioRepository.findById(cajero.getId()).orElseThrow().getPassword());
        auth.iniciar("cajero", "ClaveCajero123");
        assertThrows(AccesoDenegadoException.class, () -> categorias.registrar("No permitido"));
        assertThrows(AccesoDenegadoException.class, () -> usuarios.listar());
        assertNotNull(clientes.registrar(DatosPrueba.cliente()).getId());
        auth.iniciar("admin", "ClaveSoloPruebas123");
        usuarios.cambiarPassword(cajero.getId(), "NuevaClaveCajero123");
        assertThrows(AccesoDenegadoException.class, () -> auth.iniciar("cajero", "ClaveCajero123"));
        auth.iniciar("admin", "ClaveSoloPruebas123");
        usuarios.desactivar(cajero.getId());
        assertThrows(AccesoDenegadoException.class, () -> auth.iniciar("cajero", "NuevaClaveCajero123"));
    }

    @Test void configuracionInicialNoSePuedeRepetir() {
        assertThrows(DatosInvalidosException.class,
                () -> auth.configurarAdministrador("Otro", "otro", "OtraClave123"));
        assertEquals(1, usuarioRepository.count());
    }
}
