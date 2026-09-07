package com.comidasrapidas;

import com.comidasrapidas.config.AppConfig;
import com.comidasrapidas.controller.LoginController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class DesktopApp extends Application {
    private ConfigurableApplicationContext context;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Comidas rápidas · Gestión del local");
        stage.setMinWidth(1000);
        stage.setMinHeight(720);
        Scene scene = new Scene(new StackPane(new Label("Conectando con PostgreSQL…")), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
        iniciarContexto(stage);
    }

    private void iniciarContexto(Stage stage) {
        javafx.concurrent.Task<ConfigurableApplicationContext> tarea = new javafx.concurrent.Task<>() {
            @Override
            protected ConfigurableApplicationContext call() {
                return new SpringApplicationBuilder(AppConfig.class).web(WebApplicationType.NONE)
                        .headless(false).run(getParameters().getRaw().toArray(String[]::new));
            }
        };
        tarea.setOnSucceeded(e -> {
            context = tarea.getValue();
            context.getBean(LoginController.class).mostrar(stage);
        });
        tarea.setOnFailed(e -> mostrarFallo(stage));
        Thread hilo = new Thread(tarea, "inicio-spring");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarFallo(Stage stage) {
        Label mensaje = new Label("No se pudo iniciar la aplicación.\n"
                + "Compruebe que PostgreSQL esté disponible y revise DB_URL, DB_USER y DB_PASSWORD.\n"
                + "Cierre esta ventana, corrija la configuración y vuelva a iniciar.");
        mensaje.setWrapText(true);
        stage.getScene().setRoot(new StackPane(mensaje));
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }
}
