package com.comidasrapidas.controller;

import com.comidasrapidas.util.Validacion;
import java.math.BigDecimal;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class Controles {
    private Controles() {
    }

    public static Button boton(String texto, Runnable accion) {
        Button boton = new Button(texto);
        boton.setOnAction(e -> Dialogos.ejecutar(accion));
        return boton;
    }

    public static Button principal(String texto, Runnable accion) {
        Button boton = boton(texto, accion);
        boton.getStyleClass().add("primary");
        return boton;
    }

    public static VBox pagina(String titulo, String descripcion, Node... contenido) {
        Label cabecera = new Label(titulo);
        cabecera.getStyleClass().add("page-title");
        Label ayuda = new Label(descripcion);
        ayuda.setWrapText(true);
        ayuda.getStyleClass().add("muted");
        VBox caja = new VBox(14, cabecera, ayuda);
        caja.getChildren().addAll(contenido);
        caja.setPadding(new Insets(24));
        return caja;
    }

    public static HBox fila(Node... elementos) {
        return new HBox(10, elementos);
    }

    public static <T> void columna(TableView<T> tabla, String titulo, Function<T, ?> valor) {
        TableColumn<T, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(valor.apply(c.getValue()))));
        columna.setCellFactory(c -> celdaConAyuda());
        tabla.getColumns().add(columna);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPlaceholder(new Label("No hay registros para mostrar."));
        VBox.setVgrow(tabla, Priority.ALWAYS);
    }

    public static <T> T seleccionado(TableView<T> tabla) {
        T valor = tabla.getSelectionModel().getSelectedItem();
        Validacion.exigir(valor != null, "Seleccione un registro en la tabla.");
        return valor;
    }

    private static <T> TableCell<T, String> celdaConAyuda() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String texto, boolean vacia) {
                super.updateItem(texto, vacia);
                setText(vacia ? null : texto);
                setTooltip(vacia ? null : new Tooltip(texto));
            }
        };
    }

    public static int entero(String texto, String campo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException error) {
            throw new com.comidasrapidas.exception.DatosInvalidosException(campo + " debe ser un número entero.");
        }
    }

    public static BigDecimal decimal(String texto) {
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException error) {
            throw new com.comidasrapidas.exception.DatosInvalidosException("Ingrese un precio como 12500 o 12500,50, sin separadores de miles.");
        }
    }
}
