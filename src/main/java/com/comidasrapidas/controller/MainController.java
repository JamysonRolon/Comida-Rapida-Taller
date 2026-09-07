package com.comidasrapidas.controller;

import com.comidasrapidas.model.Rol;
import com.comidasrapidas.model.Usuario;
import com.comidasrapidas.service.CarritoService;
import com.comidasrapidas.service.SesionService;
import java.util.function.Supplier;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class MainController {
    private final ClienteController clientes;
    private final CategoriaController categorias;
    private final ProductoController productos;
    private final UsuarioController usuarios;
    private final VentaController venta;
    private final HistorialController historial;
    private final SesionService sesion;
    private final CarritoService carrito;
    private BorderPane raiz;

    public MainController(ClienteController clientes, CategoriaController categorias, ProductoController productos,
                          UsuarioController usuarios, VentaController venta, HistorialController historial,
                          SesionService sesion, CarritoService carrito) {
        this.clientes = clientes;
        this.categorias = categorias;
        this.productos = productos;
        this.usuarios = usuarios;
        this.venta = venta;
        this.historial = historial;
        this.sesion = sesion;
        this.carrito = carrito;
    }

    public void mostrar(Stage stage, Usuario usuario, Runnable salir) {
        boolean admin = usuario.getRol() == Rol.ADMINISTRADOR;
        raiz = new BorderPane();
        VBox menu = menu(usuario, admin);
        menu.getChildren().add(Controles.boton("Cerrar sesión", () -> cerrar(salir)));
        raiz.setLeft(menu);
        stage.getScene().setRoot(raiz);
        navegar(venta::vista);
    }

    private VBox menu(Usuario usuario, boolean admin) {
        Label marca = new Label("COMIDAS\nRÁPIDAS");
        marca.getStyleClass().add("brand");
        Label identidad = new Label(usuario.getNombre() + "\n" + usuario.getRol());
        identidad.setWrapText(true);
        VBox menu = new VBox(12, marca, identidad);
        menu.getStyleClass().add("sidebar");
        menu.setPadding(new Insets(24, 18, 24, 18));
        menu.setPrefWidth(220);
        agregar(menu, "Nueva venta", venta::vista);
        agregar(menu, "Ventas realizadas", historial::vista);
        agregar(menu, "Clientes", () -> clientes.vista(admin));
        agregar(menu, "Productos", () -> productos.vista(admin));
        agregar(menu, "Categorías", () -> categorias.vista(admin));
        if (admin) {
            agregar(menu, "Empleados", usuarios::vista);
        }
        return menu;
    }

    private void agregar(VBox menu, String texto, Supplier<Parent> vista) {
        javafx.scene.control.Button boton = Controles.boton(texto, () -> navegar(vista));
        boton.setMaxWidth(Double.MAX_VALUE);
        menu.getChildren().add(boton);
    }

    private void navegar(Supplier<Parent> vista) {
        raiz.setCenter(vista.get());
    }

    private void cerrar(Runnable salir) {
        if (!carrito.getLineas().isEmpty() && !Dialogos.confirmar("¿Cerrar sesión y descartar el carrito?")) {
            return;
        }
        carrito.vaciar();
        sesion.cerrar();
        salir.run();
    }
}
