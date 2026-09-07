package com.comidasrapidas.service;

import com.comidasrapidas.config.AppConfig;
import com.comidasrapidas.controller.*;
import com.comidasrapidas.model.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AppConfig.class, properties = "logging.level.org.hibernate.SQL=WARN")
@EnabledIfEnvironmentVariable(named = "RUN_FX_IT", matches = "true")
class VisualIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired AuthService auth;
    @Autowired SesionService sesion;
    @Autowired CategoriaService categorias;
    @Autowired ProductoService productos;
    @Autowired ClienteService clientes;
    @Autowired CarritoService carrito;
    @Autowired VentaService ventas;
    @Autowired MainController main;
    @Autowired LoginController login;
    @Autowired ProductoController productoVista;
    @Autowired ClienteController clienteVista;
    @Autowired CategoriaController categoriaVista;
    @Autowired UsuarioController usuarioVista;
    @Autowired VentaController ventaVista;
    @Autowired HistorialController historialVista;
    private Stage stage;

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        PostgresIT.configurar(registry);
    }

    @Test void renderizaPantallasConDatosDePrueba() throws Exception {
        jdbc.execute("TRUNCATE detalle_venta, venta, producto, categoria, cliente, usuario RESTART IDENTITY CASCADE");
        auth.configurarAdministrador("Administrador de prueba", "admin", "ClaveSoloPruebas123");
        Usuario admin = auth.iniciar("admin", "ClaveSoloPruebas123");
        prepararDatos();
        CompletableFuture<Void> inicio = new CompletableFuture<>();
        Platform.startup(() -> inicio.complete(null));
        inicio.get(10, TimeUnit.SECONDS);
        try {
            fx(() -> {
                stage = new Stage();
                Scene escena = new Scene(new javafx.scene.layout.StackPane(), 1200, 800);
                escena.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());
                stage.setScene(escena);
                stage.setTitle("Comidas rápidas · Verificación visual");
                main.mostrar(stage, admin, () -> { });
                stage.show();
                return null;
            });
            guardar("01-venta");
            pantalla(() -> productoVista.vista(true), "02-productos");
            pantalla(() -> clienteVista.vista(true), "03-clientes");
            pantalla(() -> categoriaVista.vista(true), "04-categorias");
            pantalla(usuarioVista::vista, "05-empleados");
            pantalla(historialVista::vista, "06-historial");
            fx(() -> { login.mostrar(stage); return null; });
            guardar("07-login");
        } finally {
            fx(() -> { stage.close(); return null; });
            Platform.exit();
        }
    }

    private void prepararDatos() {
        Categoria categoria = categorias.registrar("Hamburguesas");
        Producto producto = productos.registrar(new Producto("Hamburguesa clásica", "Carne, queso y vegetales",
                new BigDecimal("12000"), 25, categoria));
        Categoria bebidas = categorias.registrar("Bebidas");
        productos.registrar(new Producto("Limonada", "Vaso individual", new BigDecimal("5000"), 30, bebidas));
        clientes.registrar(new Cliente("CC", "PRUEBA001", "Ana", "Ejemplo", "", "ana@example.com"));
        ventas.registrar(java.util.List.of(new LineaCarrito(producto.getId(), producto.getNombre(), 2, producto.getPrecio())), null);
        carrito.agregar(productos.consultar(producto.getId()), 1);
    }

    private void pantalla(Supplier<Parent> vista, String archivo) throws Exception {
        fx(() -> {
            ((javafx.scene.layout.BorderPane) stage.getScene().getRoot()).setCenter(vista.get());
            return null;
        });
        guardar(archivo);
    }

    private void guardar(String nombre) throws Exception {
        Thread.sleep(250);
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (fx(() -> stage.getScene().getRoot().isDisabled()) && System.nanoTime() < limite) {
            Thread.sleep(100);
        }
        assertFalse(fx(() -> stage.getScene().getRoot().isDisabled()), "La vista debe terminar su carga");
        WritableImage captura = fx(() -> stage.getScene().snapshot(null));
        BufferedImage imagen = new BufferedImage((int) captura.getWidth(), (int) captura.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < imagen.getHeight(); y++) {
            for (int x = 0; x < imagen.getWidth(); x++) {
                imagen.setRGB(x, y, captura.getPixelReader().getArgb(x, y));
            }
        }
        Path directorio = Files.createDirectories(Path.of("target", "capturas"));
        ImageIO.write(imagen, "png", directorio.resolve(nombre + ".png").toFile());
        assertTrue(Files.size(directorio.resolve(nombre + ".png")) > 1000);
    }

    private static <T> T fx(Callable<T> operacion) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(operacion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }
}
