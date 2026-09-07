package com.comidasrapidas.controller;

import com.comidasrapidas.model.*;
import com.comidasrapidas.service.*;
import com.comidasrapidas.util.Formato;
import java.util.List;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

@Component
public class VentaController {
    private final ProductoService productos;
    private final ClienteService clientes;
    private final VentaService ventas;
    private final CarritoService carrito;
    private final FxTasks tareas;
    private final ComprobanteController comprobantes;
    private ComboBox<Producto> producto;
    private ComboBox<Cliente> cliente;
    private TextField cantidad;
    private TableView<LineaCarrito> tabla;
    private Label total;
    private Label stock;
    private VBox pagina;

    public VentaController(ProductoService productos, ClienteService clientes, VentaService ventas,
                           CarritoService carrito, FxTasks tareas, ComprobanteController comprobantes) {
        this.productos = productos;
        this.clientes = clientes;
        this.ventas = ventas;
        this.carrito = carrito;
        this.tareas = tareas;
        this.comprobantes = comprobantes;
    }

    public Parent vista() {
        Formulario form = crearFormulario();
        tabla = new TableView<>();
        columnas();
        total = new Label();
        total.getStyleClass().add("total");
        pagina = Controles.pagina("Nueva venta", "Seleccione productos y confirme el cobro en efectivo.", form,
                Controles.fila(Controles.principal("Agregar producto", this::agregar),
                        Controles.boton("Actualizar catálogo y clientes", this::cargar)),
                tabla, Controles.fila(Controles.boton("Quitar línea", this::quitar),
                        Controles.boton("Vaciar carrito", this::vaciar)), total,
                Controles.principal("Confirmar venta en efectivo", this::confirmar));
        refrescar();
        javafx.application.Platform.runLater(this::cargar);
        return pagina;
    }

    private Formulario crearFormulario() {
        Formulario form = new Formulario();
        producto = form.opciones("Producto");
        producto.setPromptText("Seleccione un producto");
        cantidad = form.texto("Cantidad");
        cantidad.setText("1");
        stock = new Label("Seleccione un producto para ver precio y existencias.");
        form.agregar("Disponibilidad", stock);
        cliente = form.opciones("Cliente opcional");
        cliente.setPromptText("Sin cliente registrado");
        form.agregar("", Controles.boton("Venta sin cliente", () -> cliente.setValue(null)));
        producto.valueProperty().addListener((o, a, p) -> stock.setText(p == null ? ""
                : Formato.dinero(p.getPrecio()) + " · " + p.getStock() + " unidades disponibles"));
        return form;
    }

    private void columnas() {
        Controles.columna(tabla, "Producto", LineaCarrito::getNombre);
        Controles.columna(tabla, "Cantidad", LineaCarrito::getCantidad);
        Controles.columna(tabla, "Precio unitario", l -> Formato.dinero(l.getPrecioUnitario()));
        Controles.columna(tabla, "Subtotal", l -> Formato.dinero(l.getSubtotal()));
    }

    private void cargar() {
        tareas.ejecutar(pagina, () -> productos.listar(true), lista -> {
            producto.getItems().setAll(lista);
            producto.setValue(null);
            tareas.ejecutar(pagina, () -> clientes.listar(true), datos -> {
                cliente.getItems().setAll(datos);
                cliente.setValue(null);
            });
        });
    }

    private void agregar() {
        carrito.agregar(producto.getValue(), Controles.entero(cantidad.getText(), "Cantidad"));
        refrescar();
    }

    private void quitar() {
        carrito.quitar(Controles.seleccionado(tabla).getProductoId());
        refrescar();
    }

    private void vaciar() {
        if (Dialogos.confirmar("¿Vaciar el carrito actual?")) {
            carrito.vaciar();
            refrescar();
        }
    }

    private void refrescar() {
        tabla.getItems().setAll(carrito.getLineas());
        total.setText("Total  " + Formato.dinero(carrito.total()));
    }

    private void confirmar() {
        List<LineaCarrito> lineas = carrito.getLineas();
        com.comidasrapidas.util.Validacion.exigir(!lineas.isEmpty(), "Agregue al menos un producto.");
        Long clienteId = cliente.getValue() == null ? null : cliente.getValue().getId();
        if (!Dialogos.confirmar("¿Registrar la venta por " + Formato.dinero(carrito.total()) + " en efectivo?")) {
            return;
        }
        tareas.ejecutar(pagina, () -> ventas.registrar(lineas, clienteId), venta -> {
            carrito.vaciar();
            refrescar();
            comprobantes.mostrar(venta, pagina.getScene().getWindow());
            cargar();
        });
    }
}
