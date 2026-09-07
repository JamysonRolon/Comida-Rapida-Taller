package com.comidasrapidas.controller;

import com.comidasrapidas.service.AuthService;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class LoginController {
    private final AuthService servicioAutenticacion;
    private final FxTasks tareasFx;
    private final MainController controladorPrincipal;

    public LoginController(AuthService servicioAutenticacion, FxTasks tareasFx, MainController controladorPrincipal) {
        this.servicioAutenticacion = servicioAutenticacion;
        this.tareasFx = tareasFx;
        this.controladorPrincipal = controladorPrincipal;
    }

    public void mostrar(Stage escenario) {
        VBox espera = Controles.pagina("Comidas rápidas", "Preparando acceso…");
        escenario.getScene().setRoot(espera);
        tareasFx.ejecutar(espera, servicioAutenticacion::requiereConfiguracion, esInicial -> formulario(escenario, esInicial));
    }

    private void formulario(Stage escenario, boolean esInicial) {
        Formulario form = new Formulario();
        if (esInicial) {
            form.texto("Nombre");
        }
        form.texto("Usuario");
        form.password("Contraseña");
        Button entrar = Controles.principal(esInicial ? "Crear administrador" : "Iniciar sesión",
                () -> ingresar(escenario, form, esInicial));
        entrar.setDefaultButton(true);
        VBox contenido = Controles.pagina(esInicial ? "Configurar el establecimiento" : "Comidas rápidas",
                esInicial ? "Cree el administrador inicial. Contraseña de al menos 10 caracteres."
                        : "Gestión del local · Acceso para empleados", form, entrar);
        contenido.setMaxWidth(540);
        contenido.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        javafx.scene.layout.StackPane fondo = new javafx.scene.layout.StackPane(contenido);
        escenario.getScene().setRoot(fondo);
    }

    private void ingresar(Stage escenario, Formulario form, boolean esInicial) {
        String nombre = esInicial ? form.valor("Nombre") : "";
        String nombreUsuario = form.valor("Usuario");
        String clave = form.valor("Contraseña");
        tareasFx.ejecutar(form, () -> {
            if (esInicial) {
                servicioAutenticacion.configurarAdministrador(nombre, nombreUsuario, clave);
            }
            return servicioAutenticacion.iniciar(nombreUsuario, clave);
        }, usuarioAutenticado -> {
            form.limpiar();
            controladorPrincipal.mostrar(escenario, usuarioAutenticado, () -> mostrar(escenario));
        });
    }
}

