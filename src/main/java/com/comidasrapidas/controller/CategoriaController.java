package com.comidasrapidas.controller;

import com.comidasrapidas.model.Categoria;
import com.comidasrapidas.service.CategoriaService;
import com.comidasrapidas.util.Formato;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

@Component
public class CategoriaController {
    private final CategoriaService categorias;
    private final FxTasks tareas;
    private TableView<Categoria> tabla;
    private Formulario form;
    private VBox pagina;

    public CategoriaController(CategoriaService categorias, FxTasks tareas) {
        this.categorias = categorias;
        this.tareas = tareas;
    }

    public Parent vista(boolean administrador) {
        tabla = new TableView<>();
        Controles.columna(tabla, "Nombre", Categoria::getNombre);
        Controles.columna(tabla, "Estado", c -> Formato.estado(c.isActivo()));
        pagina = Controles.pagina("Categorías", "Organice los productos en una sola categoría.", tabla,
                Controles.boton("Actualizar lista", this::cargar));
        if (administrador) {
            agregarEditor();
        }
        javafx.application.Platform.runLater(this::cargar);
        return pagina;
    }

    private void agregarEditor() {
        form = new Formulario();
        form.texto("Nombre");
        pagina.getChildren().addAll(form, Controles.fila(
                Controles.principal("Guardar categoría", this::guardar),
                Controles.boton("Nueva / limpiar", this::limpiar),
                Controles.boton("Desactivar seleccionada", this::desactivar)));
        tabla.getSelectionModel().selectedItemProperty().addListener((o, a, actual) -> {
            if (actual != null) {
                form.poner("Nombre", actual.getNombre());
            }
        });
    }

    private void cargar() {
        tareas.ejecutar(pagina, () -> categorias.listar(false), lista -> tabla.getItems().setAll(lista));
    }

    private void guardar() {
        Categoria elegida = tabla.getSelectionModel().getSelectedItem();
        String nombre = form.valor("Nombre");
        tareas.ejecutar(pagina, () -> elegida == null ? categorias.registrar(nombre)
                : categorias.actualizar(elegida.getId(), nombre), guardada -> {
                    limpiar();
                    cargar();
                });
    }

    private void desactivar() {
        Categoria categoria = Controles.seleccionado(tabla);
        if (Dialogos.confirmar("¿Desactivar la categoría " + categoria.getNombre() + "?")) {
            tareas.ejecutar(pagina, () -> { categorias.desactivar(categoria.getId()); return true; }, ok -> cargar());
        }
    }

    private void limpiar() {
        tabla.getSelectionModel().clearSelection();
        form.limpiar();
    }
}
