package com.comidasrapidas.controller;

import com.comidasrapidas.model.Venta;
import com.comidasrapidas.service.ComprobanteService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class ComprobanteController {
    private final ComprobanteService comprobantes;
    private final FxTasks tareas;

    public ComprobanteController(ComprobanteService comprobantes, FxTasks tareas) {
        this.comprobantes = comprobantes;
        this.tareas = tareas;
    }

    public void mostrar(Venta venta, javafx.stage.Window propietario) {
        String texto = comprobantes.generar(venta);
        TextArea detalle = new TextArea(texto);
        detalle.setEditable(false);
        detalle.setWrapText(true);
        detalle.getStyleClass().add("receipt");
        Stage ventana = new Stage();
        ventana.initOwner(propietario);
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.setTitle("Comprobante · Venta " + venta.getId());
        VBox contenido = Controles.pagina("Venta n.º " + venta.getId(), "Comprobante interno · Efectivo", detalle,
                Controles.boton("Guardar comprobante .txt", () -> guardar(ventana, venta.getId(), texto)));
        VBox.setVgrow(detalle, javafx.scene.layout.Priority.ALWAYS);
        Scene escena = new Scene(contenido, 560, 620);
        escena.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());
        ventana.setScene(escena);
        ventana.show();
    }

    private void guardar(Stage ventana, Long ventaId, String texto) {
        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar comprobante interno");
        selector.setInitialFileName("venta-" + ventaId + ".txt");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Texto UTF-8", "*.txt"));
        java.io.File archivo = selector.showSaveDialog(ventana);
        if (archivo == null) {
            return;
        }
        tareas.ejecutar(ventana.getScene().getRoot(),
                () -> Files.writeString(archivo.toPath(), texto, StandardCharsets.UTF_8),
                ruta -> Dialogos.informar("Comprobante guardado."));
    }
}
