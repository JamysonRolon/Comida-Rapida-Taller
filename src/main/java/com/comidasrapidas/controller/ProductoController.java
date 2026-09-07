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
    private final ProductoService productos;
    private final CategoriaService categorias;
    private final FxTasks tareas;
    private TableView<Producto> tabla;
    private Formulario form;
    private ComboBox<Categoria> categoria;
    private VBox pagina;

    public ProductoController(ProductoService productos, CategoriaService categorias, FxTasks tareas) {
        this.productos = productos;
        this.categorias = categorias;
        this.tareas = tareas;
    }

    public Parent vista(boolean admin) {
        form = null;
        categoria = null;
        tabla = new TableView<>();
        columnas();
        HBox cuerpo = Controles.fila(tabla);
        HBox.setHgrow(tabla, Priority.ALWAYS);
        VBox.setVgrow(cuerpo, Priority.ALWAYS);
        if (admin) {
            cuerpo.getChildren().add(editor());
        }
        pagina = Controles.pagina("Productos", "Precio único · Stock por unidades · Desactivación sin borrar historial",
                Controles.boton("Actualizar lista", this::cargar), cuerpo);
        javafx.application.Platform.runLater(this::cargar);
        return pagina;
    }

    private void columnas() {
        Controles.columna(tabla, "Producto", Producto::getNombre);
        Controles.columna(tabla, "Categoría", p -> p.getCategoria().getNombre());
        Controles.columna(tabla, "Precio", p -> Formato.dinero(p.getPrecio()));
        Controles.columna(tabla, "Stock", Producto::getStock);
        Controles.columna(tabla, "Estado", p -> Formato.estado(p.isActivo()));
        tabla.getColumns().get(0).setMinWidth(165);
        tabla.getColumns().get(3).setMaxWidth(65);
    }

    private VBox editor() {
        form = new Formulario();
        form.texto("Nombre");
        form.texto("Descripción");
        form.texto("Precio");
        form.texto("Stock");
        categoria = form.opciones("Categoría");
        VBox editor = new VBox(12, form, Controles.principal("Guardar producto", this::guardar),
                Controles.boton("Nuevo / limpiar", this::limpiar),
                Controles.boton("Desactivar seleccionado", this::desactivar));
        editor.setPrefWidth(330);
        tabla.getSelectionModel().selectedItemProperty().addListener((o, a, actual) -> seleccionar(actual));
        return editor;
    }

    private void cargar() {
        tareas.ejecutar(pagina, () -> productos.listar(false), lista -> {
            tabla.getItems().setAll(lista);
            if (form != null) {
                tareas.ejecutar(pagina, () -> categorias.listar(true), cat -> categoria.getItems().setAll(cat));
            }
        });
    }

    private void seleccionar(Producto producto) {
        if (producto == null) {
            return;
        }
        form.poner("Nombre", producto.getNombre());
        form.poner("Descripción", producto.getDescripcion());
        form.poner("Precio", producto.getPrecio().toPlainString());
        form.poner("Stock", String.valueOf(producto.getStock()));
        categoria.getItems().stream().filter(c -> c.getId().equals(producto.getCategoria().getId()))
                .findFirst().ifPresentOrElse(categoria::setValue, () -> categoria.setValue(null));
    }

    private void guardar() {
        Producto elegido = tabla.getSelectionModel().getSelectedItem();
        Producto datos = new Producto(form.valor("Nombre"), form.valor("Descripción"),
                Controles.decimal(form.valor("Precio")), Controles.entero(form.valor("Stock"), "Stock"), categoria.getValue());
        tareas.ejecutar(pagina, () -> elegido == null ? productos.registrar(datos)
                : productos.actualizar(elegido.getId(), datos, elegido.getStock()), guardado -> {
                    limpiar();
                    cargar();
                });
    }

    private void desactivar() {
        Producto producto = Controles.seleccionado(tabla);
        if (Dialogos.confirmar("¿Desactivar " + producto.getNombre() + "?")) {
            tareas.ejecutar(pagina, () -> { productos.desactivar(producto.getId()); return true; }, ok -> cargar());
        }
    }

    private void limpiar() {
        tabla.getSelectionModel().clearSelection();
        form.limpiar();
        categoria.setValue(null);
    }
}
