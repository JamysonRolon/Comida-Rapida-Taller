package com.comidasrapidas.controller;

import com.comidasrapidas.model.Venta;
import com.comidasrapidas.service.ReporteExcelService;
import com.comidasrapidas.service.VentaService;
import com.comidasrapidas.util.Formato;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;

@Component
public class HistorialController {
    private final VentaService servicioVentas;
    private final FxTasks tareasFx;
    private final ComprobanteController controladorComprobantes;
    private final ReporteExcelService reporteExcel;
    private final Clock reloj;
    private DatePicker selectorFecha;
    private TextField campoNumeroVenta;
    private TextField campoNombre;
    private TextField campoFechaHora;
    private TableView<Venta> tablaVentas;
    private Label etiquetaTotal;
    private VBox panelPagina;
    private final ObservableList<Venta> datosVentas = FXCollections.observableArrayList();
    private final FilteredList<Venta> ventasFiltradas = new FilteredList<>(datosVentas);

    public HistorialController(VentaService servicioVentas, FxTasks tareasFx,
                               ComprobanteController controladorComprobantes,
                               ReporteExcelService reporteExcel, Clock reloj) {
        this.servicioVentas = servicioVentas;
        this.tareasFx = tareasFx;
        this.controladorComprobantes = controladorComprobantes;
        this.reporteExcel = reporteExcel;
        this.reloj = reloj;
    }

    public Parent vista() {
        selectorFecha = new DatePicker(LocalDate.now(reloj));
        selectorFecha.setEditable(false);

        campoNumeroVenta = new TextField();
        campoNumeroVenta.setPromptText("N.º venta");
        campoNumeroVenta.setPrefWidth(100);

        campoNombre = new TextField();
        campoNombre.setPromptText("Buscar cliente, empleado o producto");
        campoNombre.setPrefWidth(260);

        campoFechaHora = new TextField();
        campoFechaHora.setPromptText("Buscar fecha u hora");
        campoFechaHora.setPrefWidth(180);

        campoNumeroVenta.textProperty().addListener((obs, ant, act) -> aplicarFiltros());
        campoNombre.textProperty().addListener((obs, ant, act) -> aplicarFiltros());
        campoFechaHora.textProperty().addListener((obs, ant, act) -> aplicarFiltros());

        tablaVentas = new TableView<>();
        tablaVentas.setItems(ventasFiltradas);
        Controles.columna(tablaVentas, "N.º venta", Venta::getId);
        Controles.columna(tablaVentas, "Fecha y hora", venta -> Formato.fecha(venta.getFechaHora()));
        Controles.columna(tablaVentas, "Cliente", venta -> venta.getCliente() == null ? "Sin registro (POS)" : venta.getCliente().getNombre() + " " + venta.getCliente().getApellido());
        Controles.columna(tablaVentas, "Empleado", venta -> venta.getUsuario().getNombre());
        Controles.columna(tablaVentas, "Productos", Venta::getResumenProductos);
        Controles.columna(tablaVentas, "Total", venta -> Formato.dinero(venta.getTotal()));

        if (!tablaVentas.getColumns().isEmpty()) {
            tablaVentas.getColumns().get(0).setMaxWidth(85);
            if (tablaVentas.getColumns().size() > 4) {
                tablaVentas.getColumns().get(4).setMinWidth(240);
            }
        }

        etiquetaTotal = new Label();
        etiquetaTotal.getStyleClass().add("total");

        HBox filaFecha = Controles.fila(
                new Label("Fecha base:"),
                selectorFecha,
                Controles.principal("Consultar fecha", this::cargar),
                Controles.boton("Cargar todas las ventas", this::cargarTodas)
        );

        HBox filaFiltros = Controles.fila(
                new Label("Filtros:"),
                campoNumeroVenta,
                campoNombre,
                campoFechaHora,
                Controles.boton("Limpiar filtros", this::limpiarFiltros)
        );

        HBox.setHgrow(campoNombre, Priority.ALWAYS);

        panelPagina = Controles.pagina("Ventas realizadas",
                "Consulte por fecha, filtre por número de venta, nombre o fecha/hora, descargue reportes en Excel o recupere comprobantes en PDF.",
                filaFecha,
                filaFiltros,
                tablaVentas,
                etiquetaTotal,
                Controles.fila(
                        Controles.principal("Ver comprobante en PDF", this::mostrarComprobante),
                        Controles.boton("Excel del día (.xlsx)", this::exportarExcelDia),
                        Controles.boton("Excel del mes (.xlsx)", this::exportarExcelMes),
                        Controles.boton("Excel de todas las ventas (.xlsx)", this::exportarExcelTodas)
                ));

        javafx.application.Platform.runLater(this::cargar);
        return panelPagina;
    }

    private void aplicarFiltros() {
        String numFiltro = campoNumeroVenta != null && campoNumeroVenta.getText() != null ? campoNumeroVenta.getText().trim() : "";
        String nombreFiltro = campoNombre != null && campoNombre.getText() != null ? campoNombre.getText().trim().toLowerCase(Locale.ROOT) : "";
        String fechaHoraFiltro = campoFechaHora != null && campoFechaHora.getText() != null ? campoFechaHora.getText().trim().toLowerCase(Locale.ROOT) : "";

        ventasFiltradas.setPredicate(venta -> {
            if (!numFiltro.isEmpty()) {
                if (!String.valueOf(venta.getId()).contains(numFiltro)) {
                    return false;
                }
            }
            if (!nombreFiltro.isEmpty()) {
                String cliente = venta.getCliente() != null
                        ? (venta.getCliente().getNombre() + " " + (venta.getCliente().getApellido() != null ? venta.getCliente().getApellido() : "")).toLowerCase(Locale.ROOT)
                        : "sin registro (pos)";
                String empleado = venta.getUsuario() != null ? venta.getUsuario().getNombre().toLowerCase(Locale.ROOT) : "";
                String productos = venta.getResumenProductos().toLowerCase(Locale.ROOT);
                if (!cliente.contains(nombreFiltro) && !empleado.contains(nombreFiltro) && !productos.contains(nombreFiltro)) {
                    return false;
                }
            }
            if (!fechaHoraFiltro.isEmpty()) {
                String fechaHoraStr = Formato.fecha(venta.getFechaHora()).toLowerCase(Locale.ROOT);
                if (!fechaHoraStr.contains(fechaHoraFiltro)) {
                    return false;
                }
            }
            return true;
        });
        actualizarTotalVisible();
    }

    private void limpiarFiltros() {
        if (campoNumeroVenta != null) campoNumeroVenta.clear();
        if (campoNombre != null) campoNombre.clear();
        if (campoFechaHora != null) campoFechaHora.clear();
        aplicarFiltros();
    }

    private void actualizarTotalVisible() {
        if (etiquetaTotal == null) {
            return;
        }
        BigDecimal totalVisible = ventasFiltradas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        etiquetaTotal.setText("Total visible (" + ventasFiltradas.size() + " " + (ventasFiltradas.size() == 1 ? "venta" : "ventas") + "):  " + Formato.dinero(totalVisible));
    }

    private void cargar() {
        LocalDate dia = selectorFecha.getValue();
        tareasFx.ejecutar(panelPagina, () -> servicioVentas.consultarDia(dia), lista -> {
            datosVentas.setAll(lista);
            limpiarFiltros();
        });
    }

    private void cargarTodas() {
        tareasFx.ejecutar(panelPagina, servicioVentas::consultarTodas, lista -> {
            datosVentas.setAll(lista);
            limpiarFiltros();
        });
    }

    private void mostrarComprobante() {
        Long id = Controles.seleccionado(tablaVentas).getId();
        tareasFx.ejecutar(panelPagina, () -> servicioVentas.consultar(id),
                venta -> controladorComprobantes.mostrar(venta, panelPagina.getScene().getWindow()));
    }

    private void exportarExcelDia() {
        LocalDate dia = selectorFecha.getValue();
        guardarExcel("ventas-dia-" + dia + ".xlsx", "Día " + dia, () -> servicioVentas.consultarDia(dia));
    }

    private void exportarExcelMes() {
        LocalDate dia = selectorFecha.getValue();
        java.time.YearMonth mes = java.time.YearMonth.from(dia);
        guardarExcel("ventas-mes-" + mes + ".xlsx", "Mes " + mes, () -> servicioVentas.consultarMes(dia));
    }

    private void exportarExcelTodas() {
        guardarExcel("ventas-todas.xlsx", "Histórico Completo", servicioVentas::consultarTodas);
    }

    private void guardarExcel(String nombreArchivoPorDefecto, String periodo, java.util.concurrent.Callable<List<Venta>> consultaVentas) {
        FileChooser selector = new FileChooser();
        selector.setTitle("Descargar reporte en formato Excel");
        selector.setInitialFileName(nombreArchivoPorDefecto);
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"));
        java.io.File archivo = selector.showSaveDialog(panelPagina.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        tareasFx.ejecutar(panelPagina, () -> {
            List<Venta> ventasObtenidas = consultaVentas.call();
            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                reporteExcel.exportarReporte(periodo, ventasObtenidas, fos);
            }
            return ventasObtenidas.size();
        }, totalExportadas -> Dialogos.informar("Reporte Excel generado exitosamente con " + totalExportadas + " ventas."));
    }
}
