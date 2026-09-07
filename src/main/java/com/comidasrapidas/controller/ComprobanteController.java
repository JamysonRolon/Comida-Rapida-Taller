package com.comidasrapidas.controller;

import com.comidasrapidas.model.Venta;
import com.comidasrapidas.service.ComprobanteService;
import java.io.FileOutputStream;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class ComprobanteController {
    private final ComprobanteService servicioComprobantes;
    private final FxTasks tareasFx;

    public ComprobanteController(ComprobanteService servicioComprobantes, FxTasks tareasFx) {
        this.servicioComprobantes = servicioComprobantes;
        this.tareasFx = tareasFx;
    }

    public void mostrar(Venta venta, javafx.stage.Window propietario) {
        boolean esFacturaElectronica = venta.getCliente() != null;
        String texto = servicioComprobantes.generar(venta);
        TextArea detalle = new TextArea(texto);
        detalle.setEditable(false);
        detalle.setWrapText(true);
        detalle.getStyleClass().add("receipt");
        Stage ventana = new Stage();
        ventana.initOwner(propietario);
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.setTitle((esFacturaElectronica ? "Factura Electrónica" : "Tiquete POS") + " · Venta " + venta.getId());
        VBox contenido = Controles.pagina("Venta n.º " + venta.getId(),
                esFacturaElectronica ? "Factura Electrónica generada · Efectivo" : "Tiquete POS · Mostrador", detalle,
                Controles.principal("Descargar comprobante en PDF", () -> guardarPdf(ventana, venta)));
        VBox.setVgrow(detalle, javafx.scene.layout.Priority.ALWAYS);
        Scene escena = new Scene(contenido, 560, 620);
        escena.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());
        ventana.setScene(escena);
        ventana.show();
    }

    private void guardarPdf(Stage ventana, Venta venta) {
        FileChooser selector = new FileChooser();
        boolean esFactura = venta.getCliente() != null;
        selector.setTitle(esFactura ? "Guardar Factura Electrónica en PDF" : "Guardar Tiquete POS en PDF");
        selector.setInitialFileName((esFactura ? "factura-electronica-" : "tiquete-pos-") + venta.getId() + ".pdf");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF (*.pdf)", "*.pdf"));
        java.io.File archivo = selector.showSaveDialog(ventana);
        if (archivo == null) {
            return;
        }
        tareasFx.ejecutar(ventana.getScene().getRoot(), () -> {
            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                servicioComprobantes.generarPdf(venta, fos);
            }
            return true;
        }, ok -> Dialogos.informar("Comprobante en PDF guardado exitosamente."));
    }
}


