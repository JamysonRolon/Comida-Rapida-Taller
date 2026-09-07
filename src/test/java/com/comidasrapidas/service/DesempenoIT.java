package com.comidasrapidas.service;

import com.comidasrapidas.config.AppConfig;
import com.comidasrapidas.model.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AppConfig.class, properties = "logging.level.org.hibernate.SQL=WARN")
@EnabledIfEnvironmentVariable(named = "RUN_PERF_IT", matches = "true")
class DesempenoIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired AuthService auth;
    @Autowired CategoriaService categorias;
    @Autowired ClienteService clientes;
    @Autowired ProductoService productos;
    private Categoria categoria;
    private final List<String> resultados = new ArrayList<>();

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        PostgresIT.configurar(registry);
    }

    @Test void mideCrudConMilClientesYMilProductos() throws Exception {
        jdbc.execute("TRUNCATE detalle_venta, venta, producto, categoria, cliente, usuario RESTART IDENTITY CASCADE");
        auth.configurarAdministrador("Prueba de tiempos", "admin", "ClaveSoloPruebas123");
        auth.iniciar("admin", "ClaveSoloPruebas123");
        categoria = categorias.registrar("Pruebas");
        jdbc.update("INSERT INTO producto(nombre, descripcion, precio, stock, activo, categoria_id) "
                + "SELECT 'Producto ' || i, '', 12000, 100, true, ? FROM generate_series(1,1000) i", categoria.getId());
        jdbc.execute("INSERT INTO cliente(tipo_documento, numero_documento, nombre, apellido, telefono, correo, activo) "
                + "SELECT 'CC', 'P' || i, 'Cliente ' || i, '', '', '', true FROM generate_series(1,1000) i");
        resultados.add("operacion;muestras;calentamiento;p95_ms;max_ms;criterio_ms");
        medir("Consultar 1000 productos", () -> productos.listar(false));
        medir("Consultar 1000 clientes", () -> clientes.listar(false));
        AtomicInteger contador = new AtomicInteger();
        medir("Registrar cliente", () -> clientes.registrar(new Cliente("CC", "N" + contador.incrementAndGet(), "Prueba", "", "", "")));
        medir("Actualizar cliente", () -> clientes.actualizar(1L, new Cliente("CC", "P1", "Editado", "", "", "")));
        AtomicInteger clienteId = new AtomicInteger(1);
        medir("Desactivar cliente", () -> clientes.desactivar((long) clienteId.getAndIncrement()));
        medir("Registrar producto", () -> productos.registrar(new Producto("Nuevo", "", new BigDecimal("1000"), 5, categoria)));
        medir("Actualizar producto", () -> productos.actualizar(1L, new Producto("Editado", "", new BigDecimal("1000"), 100, categoria), 100));
        AtomicInteger productoId = new AtomicInteger(1);
        medir("Desactivar producto", () -> productos.desactivar((long) productoId.getAndIncrement()));
        Files.write(Path.of("target", "rendimiento.csv"), resultados, StandardCharsets.UTF_8);
    }

    private void medir(String nombre, Runnable operacion) {
        for (int i = 0; i < 5; i++) {
            operacion.run();
        }
        double[] duraciones = new double[30];
        for (int i = 0; i < duraciones.length; i++) {
            long inicio = System.nanoTime();
            operacion.run();
            duraciones[i] = (System.nanoTime() - inicio) / 1_000_000.0;
        }
        Arrays.sort(duraciones);
        double p95 = duraciones[28];
        resultados.add(String.format(Locale.ROOT, "%s;30;5;%.3f;%.3f;1000", nombre, p95, duraciones[29]));
        assertTrue(p95 <= 1000, nombre + ": p95 = " + p95 + " ms");
    }
}
