package com.comidasrapidas.controller;

import com.comidasrapidas.model.Categoria;
import com.comidasrapidas.service.CategoriaService;
import com.comidasrapidas.util.Formato;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

@Component
public class CategoriaController {
    private final CategoriaService servicioCategorias;
    private final FxTasks tareasFx;
    private TableView<Categoria> tablaCategorias;
    private Formulario formulario;
    private VBox panelPagina;

    public CategoriaController(CategoriaService servicioCategorias, FxTasks tareasFx) {
        this.servicioCategorias = servicioCategorias;
        this.tareasFx = tareasFx;
    }

    public Parent vista(boolean esAdministrador) {
        tablaCategorias = new TableView<>();
        Controles.columna(tablaCategorias, "Nombre", Categoria::getNombre);

        HBox cuerpo = Controles.fila(tablaCategorias);
        HBox.setHgrow(tablaCategorias, Priority.ALWAYS);
        VBox.setVgrow(cuerpo, Priority.ALWAYS);

        if (esAdministrador) {
            cuerpo.getChildren().add(crearEditor());
        }

        panelPagina = Controles.pagina("Categorías", "Organice los productos en una sola categoría.",
                Controles.boton("Actualizar lista", this::cargar), cuerpo);

        javafx.application.Platform.runLater(this::cargar);
        return panelPagina;
    }

    private VBox crearEditor() {
        formulario = new Formulario();
        formulario.texto("Nombre");

        VBox editor = new VBox(12, formulario,
                Controles.principal("Guardar categoría", this::guardar),
                Controles.boton("Limpiar campos", this::limpiar));
        editor.setPrefWidth(300);


        tablaCategorias.getSelectionModel().selectedItemProperty().addListener((observable, anterior, actual) -> {
            if (actual != null) {
                formulario.poner("Nombre", actual.getNombre());
            }
        });
        return editor;
    }

    private void cargar() {
        tareasFx.ejecutar(panelPagina, () -> servicioCategorias.listar(false), lista -> {
            tablaCategorias.getItems().setAll(lista);
            tablaCategorias.refresh();
            limpiar();
        });
    }

    private void guardar() {
        Categoria elegida = tablaCategorias.getSelectionModel().getSelectedItem();
        String nombre = formulario.valor("Nombre");
        tareasFx.ejecutar(panelPagina, () -> elegida == null ? servicioCategorias.registrar(nombre)
                : servicioCategorias.actualizar(elegida.getId(), nombre), guardada -> {
                    limpiar();
                    cargar();
                });
    }

    private void limpiar() {
        tablaCategorias.getSelectionModel().clearSelection();
        if (formulario != null) {
            formulario.limpiar();
        }
    }
}

