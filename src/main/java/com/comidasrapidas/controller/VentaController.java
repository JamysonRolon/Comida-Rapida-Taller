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
    private final ProductoService servicioProductos;
    private final ClienteService servicioClientes;
    private final VentaService servicioVentas;
    private final CarritoService servicioCarrito;
    private final FxTasks tareasFx;
    private final ComprobanteController controladorComprobantes;
    private ComboBox<Producto> comboProducto;
    private ComboBox<Cliente> comboCliente;
    private TextField campoCantidad;
    private TableView<LineaCarrito> tablaCarrito;
    private Label etiquetaTotal;
    private Label etiquetaStock;
    private VBox panelPagina;

    public VentaController(ProductoService servicioProductos, ClienteService servicioClientes, VentaService servicioVentas,
                           CarritoService servicioCarrito, FxTasks tareasFx, ComprobanteController controladorComprobantes) {
        this.servicioProductos = servicioProductos;
        this.servicioClientes = servicioClientes;
        this.servicioVentas = servicioVentas;
        this.servicioCarrito = servicioCarrito;
        this.tareasFx = tareasFx;
        this.controladorComprobantes = controladorComprobantes;
    }

    public Parent vista() {
        Formulario formulario = crearFormulario();
        tablaCarrito = new TableView<>();
        tablaCarrito.setPlaceholder(new Label("Carro vacío"));
        configurarColumnas();
        etiquetaTotal = new Label();
        etiquetaTotal.getStyleClass().add("total");
        panelPagina = Controles.pagina("Nueva venta", "Seleccione productos y confirme el cobro en efectivo.", formulario,
                Controles.fila(Controles.principal("Agregar producto", this::agregar),
                        Controles.boton("Actualizar catálogo y clientes", this::cargar)),
                tablaCarrito, Controles.fila(Controles.boton("Quitar línea", this::quitar),
                        Controles.boton("Vaciar carrito", this::vaciar)), etiquetaTotal,
                Controles.principal("Confirmar venta en efectivo", this::confirmar));
        refrescar();
        javafx.application.Platform.runLater(this::cargar);
        return panelPagina;
    }

    private Formulario crearFormulario() {
        Formulario formulario = new Formulario();
        comboProducto = formulario.opciones("Producto");
        comboProducto.setPromptText("Seleccione un producto");
        campoCantidad = formulario.texto("Cantidad");
        campoCantidad.setText("1");
        etiquetaStock = new Label("Seleccione un producto para ver precio y existencias.");
        formulario.agregar("Disponibilidad", etiquetaStock);
        comboCliente = formulario.opciones("Cliente (Factura Electrónica)");
        comboCliente.setPromptText("Sin cliente (Venta rápida POS)");
        formulario.agregar("", Controles.boton("Venta POS sin cliente", () -> comboCliente.setValue(null)));
        comboProducto.valueProperty().addListener((observable, anterior, producto) -> etiquetaStock.setText(producto == null ? ""
                : Formato.dinero(producto.getPrecio()) + " · " + producto.getStock() + " unidades disponibles"));
        return formulario;
    }

    private void configurarColumnas() {
        Controles.columna(tablaCarrito, "Producto", LineaCarrito::getNombre);
        Controles.columna(tablaCarrito, "Cantidad", LineaCarrito::getCantidad);
        Controles.columna(tablaCarrito, "Precio unitario", linea -> Formato.dinero(linea.getPrecioUnitario()));
        Controles.columna(tablaCarrito, "Subtotal", linea -> Formato.dinero(linea.getSubtotal()));
    }

    private void cargar() {
        tareasFx.ejecutar(panelPagina, () -> servicioProductos.listar(true), listaProductos -> {
            comboProducto.getItems().setAll(listaProductos);
            comboProducto.setValue(null);
            tareasFx.ejecutar(panelPagina, () -> servicioClientes.listar(true), listaClientes -> {
                comboCliente.getItems().setAll(listaClientes);
                comboCliente.setValue(null);
            });
        });
    }

    private void agregar() {
        servicioCarrito.agregar(comboProducto.getValue(), Controles.entero(campoCantidad.getText(), "Cantidad"));
        refrescar();
    }

    private void quitar() {
        if (servicioCarrito.getLineas().isEmpty()) {
            Dialogos.informar("El carro está vacío.");
            return;
        }
        servicioCarrito.quitar(Controles.seleccionado(tablaCarrito).getProductoId());
        refrescar();
    }

    private void vaciar() {
        if (servicioCarrito.getLineas().isEmpty()) {
            Dialogos.informar("El carro ya está vacío.");
            return;
        }
        if (Dialogos.confirmar("¿Vaciar el carrito actual?")) {
            servicioCarrito.vaciar();
            refrescar();
        }
    }


    private void refrescar() {
        tablaCarrito.getItems().setAll(servicioCarrito.getLineas());
        tablaCarrito.refresh();
        etiquetaTotal.setText("Total  " + Formato.dinero(servicioCarrito.total()));
    }

    private void confirmar() {
        List<LineaCarrito> lineas = servicioCarrito.getLineas();
        com.comidasrapidas.util.Validacion.exigir(!lineas.isEmpty(), "Agregue al menos un producto.");
        Long clienteId = comboCliente.getValue() == null ? null : comboCliente.getValue().getId();
        if (!Dialogos.confirmar("¿Registrar la venta por " + Formato.dinero(servicioCarrito.total()) + " en efectivo?")) {
            return;
        }
        tareasFx.ejecutar(panelPagina, () -> servicioVentas.registrar(lineas, clienteId), ventaRealizada -> {
            servicioCarrito.vaciar();
            refrescar();
            controladorComprobantes.mostrar(ventaRealizada, panelPagina.getScene().getWindow());
            cargar();
        });
    }
}

