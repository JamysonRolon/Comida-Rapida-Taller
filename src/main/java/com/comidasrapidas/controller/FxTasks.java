package com.comidasrapidas.controller;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import org.springframework.stereotype.Component;

@Component
public class FxTasks {
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread hilo = new Thread(runnable, "operaciones-locales");
        hilo.setDaemon(true);
        return hilo;
    });

    public <T> void ejecutar(Node origen, Callable<T> operacion, Consumer<T> resultado) {
        Scene escena = origen.getScene();
        Node bloqueo = escena == null ? origen : escena.getRoot();
        bloqueo.setDisable(true);
        if (escena != null) {
            escena.setCursor(Cursor.WAIT);
        }
        Task<T> tarea = new Task<>() {
            @Override
            protected T call() throws Exception {
                return operacion.call();
            }
        };
        tarea.setOnSucceeded(e -> completar(bloqueo, escena, () -> resultado.accept(tarea.getValue())));
        tarea.setOnFailed(e -> completar(bloqueo, escena, () -> Dialogos.error(tarea.getException())));
        executor.submit(tarea);
    }

    private void completar(Node bloqueo, Scene escena, Runnable accion) {
        bloqueo.setDisable(false);
        if (escena != null) {
            escena.setCursor(Cursor.DEFAULT);
        }
        Dialogos.ejecutar(accion);
    }

    @PreDestroy
    public void cerrar() {
        executor.shutdownNow();
    }
}
