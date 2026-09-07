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
    private final ClienteController controladorClientes;
    private final CategoriaController controladorCategorias;
    private final ProductoController controladorProductos;
    private final UsuarioController controladorUsuarios;
    private final VentaController controladorVenta;
    private final HistorialController controladorHistorial;
    private final SesionService servicioSesion;
    private final CarritoService servicioCarrito;
    private BorderPane panelRaiz;

    public MainController(ClienteController controladorClientes, CategoriaController controladorCategorias, ProductoController controladorProductos,
                          UsuarioController controladorUsuarios, VentaController controladorVenta, HistorialController controladorHistorial,
                          SesionService servicioSesion, CarritoService servicioCarrito) {
        this.controladorClientes = controladorClientes;
        this.controladorCategorias = controladorCategorias;
        this.controladorProductos = controladorProductos;
        this.controladorUsuarios = controladorUsuarios;
        this.controladorVenta = controladorVenta;
        this.controladorHistorial = controladorHistorial;
        this.servicioSesion = servicioSesion;
        this.servicioCarrito = servicioCarrito;
    }

    public void mostrar(Stage escenario, Usuario usuario, Runnable accionSalir) {
        boolean esAdministrador = usuario.getRol() == Rol.ADMINISTRADOR;
        panelRaiz = new BorderPane();
        VBox menuLateral = crearMenu(usuario, esAdministrador);
        menuLateral.getChildren().add(Controles.boton("Cerrar sesión", () -> cerrarSesion(accionSalir)));
        panelRaiz.setLeft(menuLateral);
        escenario.getScene().setRoot(panelRaiz);
        navegar(controladorVenta::vista);
    }

    private VBox crearMenu(Usuario usuario, boolean esAdministrador) {
        Label marca = new Label("COMIDAS\nRÁPIDAS");
        marca.getStyleClass().add("brand");
        Label identidad = new Label(usuario.getNombre() + "\n" + usuario.getRol());
        identidad.setWrapText(true);
        VBox menu = new VBox(12, marca, identidad);
        menu.getStyleClass().add("sidebar");
        menu.setPadding(new Insets(24, 18, 24, 18));
        menu.setPrefWidth(220);
        agregarOpcionMenu(menu, "Nueva venta", controladorVenta::vista);
        agregarOpcionMenu(menu, "Ventas realizadas", controladorHistorial::vista);
        agregarOpcionMenu(menu, "Clientes", () -> controladorClientes.vista(esAdministrador));
        agregarOpcionMenu(menu, "Productos", () -> controladorProductos.vista(esAdministrador));
        agregarOpcionMenu(menu, "Categorías", () -> controladorCategorias.vista(esAdministrador));
        if (esAdministrador) {
            agregarOpcionMenu(menu, "Empleados", controladorUsuarios::vista);
        }
        return menu;
    }

    private void agregarOpcionMenu(VBox menu, String texto, Supplier<Parent> proveedorVista) {
        javafx.scene.control.Button boton = Controles.boton(texto, () -> navegar(proveedorVista));
        boton.setMaxWidth(Double.MAX_VALUE);
        menu.getChildren().add(boton);
    }

    private void navegar(Supplier<Parent> proveedorVista) {
        panelRaiz.setCenter(proveedorVista.get());
    }

    private void cerrarSesion(Runnable accionSalir) {
        if (!servicioCarrito.getLineas().isEmpty() && !Dialogos.confirmar("¿Cerrar sesión y descartar el carrito?")) {
            return;
        }
        servicioCarrito.vaciar();
        servicioSesion.cerrar();
        accionSalir.run();
    }
}

