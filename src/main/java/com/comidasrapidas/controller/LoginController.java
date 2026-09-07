package com.comidasrapidas.controller;

import com.comidasrapidas.service.AuthService;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class LoginController {
    private final AuthService auth;
    private final FxTasks tareas;
    private final MainController principal;

    public LoginController(AuthService auth, FxTasks tareas, MainController principal) {
        this.auth = auth;
        this.tareas = tareas;
        this.principal = principal;
    }

    public void mostrar(Stage stage) {
        VBox espera = Controles.pagina("Comidas rápidas", "Preparando acceso…");
        stage.getScene().setRoot(espera);
        tareas.ejecutar(espera, auth::requiereConfiguracion, inicial -> formulario(stage, inicial));
    }

    private void formulario(Stage stage, boolean inicial) {
        Formulario form = new Formulario();
        if (inicial) {
            form.texto("Nombre");
        }
        form.texto("Usuario");
        form.password("Contraseña");
        Button entrar = Controles.principal(inicial ? "Crear administrador" : "Iniciar sesión",
                () -> ingresar(stage, form, inicial));
        entrar.setDefaultButton(true);
        VBox contenido = Controles.pagina(inicial ? "Configurar el establecimiento" : "Comidas rápidas",
                inicial ? "Cree el administrador inicial. Contraseña de al menos 10 caracteres."
                        : "Gestión del local · Acceso para empleados", form, entrar);
        contenido.setMaxWidth(540);
        contenido.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        javafx.scene.layout.StackPane fondo = new javafx.scene.layout.StackPane(contenido);
        stage.getScene().setRoot(fondo);
    }

    private void ingresar(Stage stage, Formulario form, boolean inicial) {
        String nombre = inicial ? form.valor("Nombre") : "";
        String username = form.valor("Usuario");
        String password = form.valor("Contraseña");
        tareas.ejecutar(form, () -> {
            if (inicial) {
                auth.configurarAdministrador(nombre, username, password);
            }
            return auth.iniciar(username, password);
        }, usuario -> {
            form.limpiar();
            principal.mostrar(stage, usuario, () -> mostrar(stage));
        });
    }
}
