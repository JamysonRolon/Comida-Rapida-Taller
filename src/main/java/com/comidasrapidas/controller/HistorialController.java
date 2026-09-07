package com.comidasrapidas.controller;

import com.comidasrapidas.model.Venta;
import com.comidasrapidas.service.ReporteExcelService;
import com.comidasrapidas.service.VentaService;
import com.comidasrapidas.util.Formato;
import java.io.FileOutputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import javafx.scene.Parent;
import javafx.scene.control.*;
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
    private TableView<Venta> tablaVentas;
    private Label etiquetaTotal;
    private VBox panelPagina;

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
        tablaVentas = new TableView<>();
        Controles.columna(tablaVentas, "N.º venta", Venta::getId);
        Controles.columna(tablaVentas, "Fecha y hora", venta -> Formato.fecha(venta.getFechaHora()));
        Controles.columna(tablaVentas, "Cliente", venta -> venta.getCliente() == null ? "Sin registro (POS)" : venta.getCliente().getNombre() + " " + venta.getCliente().getApellido());
        Controles.columna(tablaVentas, "Empleado", venta -> venta.getUsuario().getNombre());
        Controles.columna(tablaVentas, "Total", venta -> Formato.dinero(venta.getTotal()));
        etiquetaTotal = new Label();
        etiquetaTotal.getStyleClass().add("total");

        panelPagina = Controles.pagina("Ventas realizadas", "Consulte por fecha, descargue reportes en Excel o recupere comprobantes en PDF.",
                Controles.fila(new Label("Fecha:"), selectorFecha, Controles.principal("Consultar fecha", this::cargar)),
                tablaVentas, etiquetaTotal,
                Controles.fila(
                        Controles.principal("Ver comprobante en PDF", this::mostrarComprobante),
                        Controles.boton("Excel del día (.xlsx)", this::exportarExcelDia),
                        Controles.boton("Excel del mes (.xlsx)", this::exportarExcelMes),
                        Controles.boton("Excel de todas las ventas (.xlsx)", this::exportarExcelTodas)
                ));
        javafx.application.Platform.runLater(this::cargar);
        return panelPagina;
    }

    private void cargar() {
        LocalDate dia = selectorFecha.getValue();
        tareasFx.ejecutar(panelPagina, () -> servicioVentas.consultarDia(dia), lista -> {
            tablaVentas.getItems().setAll(lista);
            tablaVentas.refresh();
            tareasFx.ejecutar(panelPagina, () -> servicioVentas.totalDia(dia),
                    importe -> etiquetaTotal.setText("Total del día  " + Formato.dinero(importe)));
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


