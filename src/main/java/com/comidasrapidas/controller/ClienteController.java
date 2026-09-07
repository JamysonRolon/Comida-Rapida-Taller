package com.comidasrapidas.controller;

import com.comidasrapidas.model.Cliente;
import com.comidasrapidas.service.ClienteService;
import com.comidasrapidas.util.Formato;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.stereotype.Component;

@Component
public class ClienteController {
    private final ClienteService clientes;
    private final FxTasks tareas;
    private TableView<Cliente> tabla;
    private Formulario form;
    private VBox pagina;
    private boolean admin;
    private final javafx.collections.ObservableList<Cliente> datos = javafx.collections.FXCollections.observableArrayList();
    private final FilteredList<Cliente> filtrados = new FilteredList<>(datos);

    public ClienteController(ClienteService clientes, FxTasks tareas) {
        this.clientes = clientes;
        this.tareas = tareas;
    }

    public Parent vista(boolean administrador) {
        admin = administrador;
        tabla = new TableView<>();
        filtrados.setPredicate(c -> true);
        tabla.setItems(filtrados);
        crearColumnas();
        form = crearFormulario();
        VBox editor = new VBox(12, form, Controles.principal("Guardar cliente", this::guardar),
                Controles.boton("Nuevo / limpiar", this::limpiar));
        if (admin) {
            editor.getChildren().add(Controles.boton("Desactivar seleccionado", this::desactivar));
        }
        editor.setPrefWidth(340);
        HBox cuerpo = Controles.fila(tabla, editor);
        HBox.setHgrow(tabla, Priority.ALWAYS);
        VBox.setVgrow(cuerpo, Priority.ALWAYS);
        pagina = Controles.pagina("Clientes", "El cliente es opcional al vender. Puede buscar por documento o nombre.",
                buscador(), Controles.boton("Actualizar lista", this::cargar), cuerpo);
        tabla.getSelectionModel().selectedItemProperty().addListener((o, anterior, actual) -> seleccionar(actual));
        javafx.application.Platform.runLater(this::cargar);
        return pagina;
    }

    private void crearColumnas() {
        Controles.columna(tabla, "Documento", Cliente::getNumeroDocumento);
        Controles.columna(tabla, "Nombre", c -> c.getNombre() + " " + c.getApellido());
        Controles.columna(tabla, "Teléfono", Cliente::getTelefono);
        Controles.columna(tabla, "Correo", Cliente::getCorreo);
        Controles.columna(tabla, "Estado", c -> Formato.estado(c.isActivo()));
    }

    private Formulario crearFormulario() {
        Formulario nuevo = new Formulario();
        nuevo.texto("Tipo de documento");
        nuevo.texto("Documento");
        nuevo.texto("Nombre");
        nuevo.texto("Apellido");
        nuevo.texto("Teléfono");
        nuevo.texto("Correo");
        return nuevo;
    }

    private TextField buscador() {
        TextField buscar = new TextField();
        buscar.setPromptText("Buscar nombre o documento");
        buscar.textProperty().addListener((o, a, texto) -> {
            String filtro = texto.toLowerCase(java.util.Locale.ROOT);
            filtrados.setPredicate(c -> c.toString().toLowerCase(java.util.Locale.ROOT).contains(filtro));
        });
        return buscar;
    }

    private void cargar() {
        tareas.ejecutar(pagina, () -> clientes.listar(false), lista -> {
            datos.setAll(lista);
            limpiar();
        });
    }

    private void seleccionar(Cliente cliente) {
        if (cliente == null || !admin) {
            return;
        }
        form.poner("Tipo de documento", cliente.getTipoDocumento());
        form.poner("Documento", cliente.getNumeroDocumento());
        form.poner("Nombre", cliente.getNombre());
        form.poner("Apellido", cliente.getApellido());
        form.poner("Teléfono", cliente.getTelefono());
        form.poner("Correo", cliente.getCorreo());
    }

    private void guardar() {
        Cliente datosCliente = new Cliente(form.valor("Tipo de documento"), form.valor("Documento"),
                form.valor("Nombre"), form.valor("Apellido"), form.valor("Teléfono"), form.valor("Correo"));
        Cliente elegido = admin ? tabla.getSelectionModel().getSelectedItem() : null;
        tareas.ejecutar(pagina, () -> elegido == null ? clientes.registrar(datosCliente)
                : clientes.actualizar(elegido.getId(), datosCliente), guardado -> cargar());
    }

    private void desactivar() {
        Cliente cliente = Controles.seleccionado(tabla);
        if (Dialogos.confirmar("¿Desactivar a " + cliente.getNombre() + "?")) {
            tareas.ejecutar(pagina, () -> { clientes.desactivar(cliente.getId()); return true; }, ok -> cargar());
        }
    }

    private void limpiar() {
        tabla.getSelectionModel().clearSelection();
        form.limpiar();
    }
}
