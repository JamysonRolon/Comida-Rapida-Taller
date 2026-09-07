package com.comidasrapidas.controller;

import com.comidasrapidas.model.Venta;
import com.comidasrapidas.service.VentaService;
import com.comidasrapidas.util.Formato;
import java.time.Clock;
import java.time.LocalDate;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

@Component
public class HistorialController {
    private final VentaService ventas;
    private final FxTasks tareas;
    private final ComprobanteController comprobantes;
    private final Clock clock;
    private DatePicker fecha;
    private TableView<Venta> tabla;
    private Label total;
    private VBox pagina;

    public HistorialController(VentaService ventas, FxTasks tareas, ComprobanteController comprobantes, Clock clock) {
        this.ventas = ventas;
        this.tareas = tareas;
        this.comprobantes = comprobantes;
        this.clock = clock;
    }

    public Parent vista() {
        fecha = new DatePicker(LocalDate.now(clock));
        fecha.setEditable(false);
        tabla = new TableView<>();
        Controles.columna(tabla, "N.º venta", Venta::getId);
        Controles.columna(tabla, "Fecha y hora", v -> Formato.fecha(v.getFechaHora()));
        Controles.columna(tabla, "Cliente", v -> v.getCliente() == null ? "Sin registro" : v.getCliente().getNombre());
        Controles.columna(tabla, "Empleado", v -> v.getUsuario().getNombre());
        Controles.columna(tabla, "Total", v -> Formato.dinero(v.getTotal()));
        total = new Label();
        total.getStyleClass().add("total");
        pagina = Controles.pagina("Ventas realizadas", "Consulte un día y recupere sus comprobantes.",
                Controles.fila(new Label("Fecha"), fecha, Controles.principal("Consultar", this::cargar)),
                tabla, total, Controles.boton("Ver comprobante seleccionado", this::comprobante));
        javafx.application.Platform.runLater(this::cargar);
        return pagina;
    }

    private void cargar() {
        LocalDate dia = fecha.getValue();
        tareas.ejecutar(pagina, () -> ventas.consultarDia(dia), lista -> {
            tabla.getItems().setAll(lista);
            tareas.ejecutar(pagina, () -> ventas.totalDia(dia),
                    importe -> total.setText("Total del día  " + Formato.dinero(importe)));
        });
    }

    private void comprobante() {
        Long id = Controles.seleccionado(tabla).getId();
        tareas.ejecutar(pagina, () -> ventas.consultar(id),
                venta -> comprobantes.mostrar(venta, pagina.getScene().getWindow()));
    }
}
