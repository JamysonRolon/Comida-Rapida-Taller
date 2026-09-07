package com.comidasrapidas.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class Formulario extends GridPane {
    private final Map<String, TextInputControl> campos = new LinkedHashMap<>();
    private int fila;

    public Formulario() {
        setHgap(12);
        setVgap(10);
        getStyleClass().add("form");
    }

    public TextField texto(String etiqueta) {
        TextField campo = new TextField();
        campos.put(etiqueta, campo);
        agregar(etiqueta, campo);
        return campo;
    }

    public PasswordField password(String etiqueta) {
        PasswordField campo = new PasswordField();
        campos.put(etiqueta, campo);
        agregar(etiqueta, campo);
        return campo;
    }

    public <T> ComboBox<T> opciones(String etiqueta) {
        ComboBox<T> campo = new ComboBox<>();
        campo.setMaxWidth(Double.MAX_VALUE);
        agregar(etiqueta, campo);
        return campo;
    }

    public void agregar(String etiqueta, javafx.scene.Node campo) {
        Label label = new Label(etiqueta);
        label.setLabelFor(campo);
        addRow(fila++, label, campo);
        GridPane.setHgrow(campo, Priority.ALWAYS);
    }

    public String valor(String etiqueta) {
        return campos.get(etiqueta).getText();
    }

    public void poner(String etiqueta, String valor) {
        campos.get(etiqueta).setText(valor);
    }

    public void limpiar() {
        campos.values().forEach(TextInputControl::clear);
    }
}
