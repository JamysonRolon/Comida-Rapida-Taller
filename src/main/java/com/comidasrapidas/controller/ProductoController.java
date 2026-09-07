package com.comidasrapidas.controller;

import com.comidasrapidas.model.Categoria;
import com.comidasrapidas.model.Producto;
import com.comidasrapidas.service.CategoriaService;
import com.comidasrapidas.service.ProductoService;
import com.comidasrapidas.util.Formato;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.stereotype.Component;

@Component
public class ProductoController {
    private final ProductoService servicioProductos;
    private final CategoriaService servicioCategorias;
    private final FxTasks tareasFx;
    private TableView<Producto> tablaProductos;
    private Formulario formulario;
    private ComboBox<Categoria> comboCategoria;
    private VBox panelPagina;

    public ProductoController(ProductoService servicioProductos, CategoriaService servicioCategorias, FxTasks tareasFx) {
        this.servicioProductos = servicioProductos;
        this.servicioCategorias = servicioCategorias;
        this.tareasFx = tareasFx;
    }

    public Parent vista(boolean esAdministrador) {
        formulario = null;
        comboCategoria = null;
        tablaProductos = new TableView<>();
        configurarColumnas();
        HBox cuerpo = Controles.fila(tablaProductos);
        HBox.setHgrow(tablaProductos, Priority.ALWAYS);
        VBox.setVgrow(cuerpo, Priority.ALWAYS);
        if (esAdministrador) {
            cuerpo.getChildren().add(crearEditor());
        }
        panelPagina = Controles.pagina("Productos", "Precio único · Stock por unidades · Modificación de productos",
                Controles.boton("Actualizar lista", this::cargar), cuerpo);
        javafx.application.Platform.runLater(this::cargar);
        return panelPagina;
    }

    private void configurarColumnas() {
        Controles.columna(tablaProductos, "Producto", Producto::getNombre);
        Controles.columna(tablaProductos, "Categoría", producto -> producto.getCategoria().getNombre());
        Controles.columna(tablaProductos, "Precio", producto -> Formato.dinero(producto.getPrecio()));
        Controles.columna(tablaProductos, "Stock", Producto::getStock);
        tablaProductos.getColumns().get(0).setMinWidth(165);
        tablaProductos.getColumns().get(3).setMaxWidth(65);
    }

    private VBox crearEditor() {
        formulario = new Formulario();
        formulario.texto("Nombre");
        formulario.texto("Descripción");
        formulario.texto("Precio");
        formulario.texto("Stock");
        comboCategoria = formulario.opciones("Categoría");
        VBox editor = new VBox(12, formulario, Controles.principal("Guardar producto", this::guardar),
                Controles.boton("Limpiar campos", this::limpiar));
        editor.setPrefWidth(330);

        tablaProductos.getSelectionModel().selectedItemProperty().addListener((observable, anterior, actual) -> seleccionar(actual));
        return editor;
    }

    private void cargar() {
        tareasFx.ejecutar(panelPagina, () -> servicioProductos.listar(false), lista -> {
            tablaProductos.getItems().setAll(lista);
            tablaProductos.refresh();
            if (formulario != null && comboCategoria != null) {
                tareasFx.ejecutar(panelPagina, () -> servicioCategorias.listar(true), categorias -> comboCategoria.getItems().setAll(categorias));
            }
        });
    }

    private void seleccionar(Producto producto) {
        if (producto == null) {
            return;
        }
        formulario.poner("Nombre", producto.getNombre());
        formulario.poner("Descripción", producto.getDescripcion());
        formulario.poner("Precio", producto.getPrecio().toPlainString());
        formulario.poner("Stock", String.valueOf(producto.getStock()));
        comboCategoria.getItems().stream().filter(categoria -> categoria.getId().equals(producto.getCategoria().getId()))
                .findFirst().ifPresentOrElse(comboCategoria::setValue, () -> comboCategoria.setValue(null));
    }

    private void guardar() {
        Producto elegido = tablaProductos.getSelectionModel().getSelectedItem();
        Producto datos = new Producto(formulario.valor("Nombre"), formulario.valor("Descripción"),
                Controles.decimal(formulario.valor("Precio")), Controles.entero(formulario.valor("Stock"), "Stock"), comboCategoria.getValue());
        tareasFx.ejecutar(panelPagina, () -> elegido == null ? servicioProductos.registrar(datos)
                : servicioProductos.actualizar(elegido.getId(), datos, elegido.getStock()), guardado -> {
                    limpiar();
                    cargar();
                });
    }

    private void limpiar() {
        tablaProductos.getSelectionModel().clearSelection();
        if (formulario != null) {
            formulario.limpiar();
        }
        if (comboCategoria != null) {
            comboCategoria.setValue(null);
        }
    }
}

