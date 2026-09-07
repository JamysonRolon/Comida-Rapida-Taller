package com.comidasrapidas.controller;

import com.comidasrapidas.model.Cliente;
import com.comidasrapidas.service.ClienteService;
import com.comidasrapidas.util.Formato;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.stereotype.Component;

@Component
public class ClienteController {
    private static final String CEDULA_CIUDADANIA = "Cédula de ciudadanía";
    private static final String NIT = "NIT";

    private final ClienteService servicioClientes;
    private final FxTasks tareasFx;
    private TableView<Cliente> tablaClientes;
    private Formulario formulario;
    private ComboBox<String> comboTipoDocumento;
    private VBox panelPagina;
    private boolean esAdministrador;
    private final ObservableList<Cliente> datosClientes = FXCollections.observableArrayList();
    private final FilteredList<Cliente> clientesFiltrados = new FilteredList<>(datosClientes);

    public ClienteController(ClienteService servicioClientes, FxTasks tareasFx) {
        this.servicioClientes = servicioClientes;
        this.tareasFx = tareasFx;
    }

    private Button botonGuardar;
    private Button botonEliminar;

    public Parent vista(boolean administrador) {
        this.esAdministrador = administrador;
        tablaClientes = new TableView<>();
        clientesFiltrados.setPredicate(cliente -> true);
        tablaClientes.setItems(clientesFiltrados);
        configurarColumnas();
        formulario = crearFormulario();

        botonGuardar = Controles.principal("Guardar nuevo cliente", this::guardar);
        botonEliminar = Controles.boton("Eliminar cliente", this::eliminar);
        botonEliminar.setDisable(true);
        if (!esAdministrador) {
            botonEliminar.setVisible(false);
            botonEliminar.setManaged(false);
        }

        VBox editor = new VBox(12, formulario, botonGuardar, botonEliminar,
                Controles.boton("Limpiar campos", this::limpiar));
        editor.setPrefWidth(340);

        HBox cuerpo = Controles.fila(tablaClientes, editor);
        HBox.setHgrow(tablaClientes, Priority.ALWAYS);
        VBox.setVgrow(cuerpo, Priority.ALWAYS);

        panelPagina = Controles.pagina("Clientes", "El cliente es opcional al vender. Puede buscar por documento o nombre.",
                crearBuscador(), Controles.boton("Actualizar lista", this::cargar), cuerpo);

        tablaClientes.getSelectionModel().selectedItemProperty().addListener((observable, anterior, actual) -> seleccionar(actual));
        javafx.application.Platform.runLater(this::cargar);
        return panelPagina;
    }

    private void configurarColumnas() {
        Controles.columna(tablaClientes, "Tipo de documento", Cliente::getTipoDocumento);
        Controles.columna(tablaClientes, "Documento", Cliente::getNumeroDocumento);
        Controles.columna(tablaClientes, "Nombre", cliente -> cliente.getNombre() + " " + cliente.getApellido());
        Controles.columna(tablaClientes, "Teléfono", Cliente::getTelefono);
        Controles.columna(tablaClientes, "Correo", Cliente::getCorreo);
        if (!tablaClientes.getColumns().isEmpty()) {
            tablaClientes.getColumns().get(0).setMinWidth(150);
            tablaClientes.getColumns().get(1).setMinWidth(110);
        }
    }


    private Formulario crearFormulario() {
        Formulario nuevoFormulario = new Formulario();
        comboTipoDocumento = nuevoFormulario.opciones("Tipo de documento");
        comboTipoDocumento.getItems().setAll(CEDULA_CIUDADANIA, NIT);
        comboTipoDocumento.setValue(CEDULA_CIUDADANIA);

        nuevoFormulario.texto("Documento");
        nuevoFormulario.texto("Nombre");
        nuevoFormulario.texto("Apellido");
        nuevoFormulario.texto("Teléfono");
        nuevoFormulario.texto("Correo");
        return nuevoFormulario;
    }

    private TextField crearBuscador() {
        TextField campoBusqueda = new TextField();
        campoBusqueda.setPromptText("Buscar nombre o documento");
        campoBusqueda.textProperty().addListener((observable, anterior, texto) -> {
            String filtro = texto == null ? "" : texto.toLowerCase(java.util.Locale.ROOT);
            clientesFiltrados.setPredicate(cliente -> cliente.toString().toLowerCase(java.util.Locale.ROOT).contains(filtro));
        });
        return campoBusqueda;
    }

    private void cargar() {
        tareasFx.ejecutar(panelPagina, () -> servicioClientes.listar(false), lista -> {
            datosClientes.setAll(lista);
            tablaClientes.refresh();
            limpiar();
        });
    }

    private void seleccionar(Cliente cliente) {
        if (cliente == null || !esAdministrador) {
            return;
        }
        if (cliente.getTipoDocumento() != null && cliente.getTipoDocumento().equalsIgnoreCase("NIT")) {
            comboTipoDocumento.setValue(NIT);
        } else {
            comboTipoDocumento.setValue(CEDULA_CIUDADANIA);
        }
        formulario.poner("Documento", cliente.getNumeroDocumento());
        formulario.poner("Nombre", cliente.getNombre());
        formulario.poner("Apellido", cliente.getApellido());
        formulario.poner("Teléfono", cliente.getTelefono());
        formulario.poner("Correo", cliente.getCorreo());
        if (botonGuardar != null) {
            botonGuardar.setText("Actualizar cliente");
        }
        if (botonEliminar != null && esAdministrador) {
            botonEliminar.setDisable(false);
        }
    }

    private void guardar() {
        String tipoDoc = comboTipoDocumento.getValue() != null ? comboTipoDocumento.getValue() : CEDULA_CIUDADANIA;
        Cliente datosCliente = new Cliente(tipoDoc, formulario.valor("Documento"),
                formulario.valor("Nombre"), formulario.valor("Apellido"), formulario.valor("Teléfono"), formulario.valor("Correo"));
        Cliente elegido = esAdministrador ? tablaClientes.getSelectionModel().getSelectedItem() : null;
        tareasFx.ejecutar(panelPagina, () -> elegido == null ? servicioClientes.registrar(datosCliente)
                : servicioClientes.actualizar(elegido.getId(), datosCliente), guardado -> {
                    limpiar();
                    cargar();
                    Dialogos.informar(elegido == null ? "Cliente registrado exitosamente." : "Cliente actualizado exitosamente.");
                });
    }

    private void eliminar() {
        Cliente elegido = tablaClientes.getSelectionModel().getSelectedItem();
        if (elegido == null) {
            Dialogos.informar("Seleccione un cliente en la tabla para eliminar.");
            return;
        }
        String nombreCompleto = elegido.getNombre() + (elegido.getApellido() != null && !elegido.getApellido().isBlank() ? " " + elegido.getApellido() : "");
        if (Dialogos.confirmar("¿Está seguro de eliminar el cliente '" + nombreCompleto + "'?")) {
            tareasFx.ejecutar(panelPagina, () -> {
                servicioClientes.eliminar(elegido.getId());
                return true;
            }, ok -> {
                limpiar();
                cargar();
                Dialogos.informar("Cliente eliminado exitosamente.");
            });
        }
    }

    private void limpiar() {
        tablaClientes.getSelectionModel().clearSelection();
        if (formulario != null) {
            formulario.limpiar();
        }
        if (comboTipoDocumento != null) {
            comboTipoDocumento.setValue(CEDULA_CIUDADANIA);
        }
        if (botonGuardar != null) {
            botonGuardar.setText("Guardar nuevo cliente");
        }
        if (botonEliminar != null) {
            botonEliminar.setDisable(true);
        }
    }
}

