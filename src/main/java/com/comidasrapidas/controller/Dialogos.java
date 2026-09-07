package com.comidasrapidas.controller;

import com.comidasrapidas.exception.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

public final class Dialogos {
    private static final Logger LOG = LoggerFactory.getLogger(Dialogos.class);

    private Dialogos() {
    }

    public static void ejecutar(Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException error) {
            error(error);
        }
    }

    public static boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmar operación");
        alerta.setHeaderText("Revise antes de continuar");
        return alerta.showAndWait().filter(ButtonType.YES::equals).isPresent();
    }

    public static void informar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        alerta.setTitle("Comidas rápidas");
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    public static void error(Throwable error) {
        LOG.warn("Operación no completada", error);
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje(error), ButtonType.OK);
        alerta.setTitle("Revise la operación");
        alerta.setHeaderText("No se pudo completar");
        alerta.showAndWait();
    }

    private static String mensaje(Throwable error) {
        if (error instanceof DatosInvalidosException || error instanceof AccesoDenegadoException
                || error instanceof ClienteNoEncontradoException || error instanceof ProductoNoEncontradoException
                || error instanceof StockInsuficienteException) {
            return error.getMessage();
        }
        if (error instanceof DataIntegrityViolationException) {
            return "Los datos están duplicados o incumplen una restricción. Actualice la lista y revise los campos.";
        }
        return "No fue posible completar la operación. Compruebe PostgreSQL y vuelva a consultar los datos antes de repetirla.";
    }
}
