package com.comidasrapidas.controller;

import com.comidasrapidas.model.Rol;
import com.comidasrapidas.model.Usuario;
import com.comidasrapidas.service.UsuarioService;
import com.comidasrapidas.util.Formato;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

@Component
public class UsuarioController {
    private final UsuarioService servicioUsuarios;
    private final FxTasks tareasFx;
    private TableView<Usuario> tablaUsuarios;
    private Formulario formulario;
    private ComboBox<Rol> comboRol;
    private VBox panelPagina;

    public UsuarioController(UsuarioService servicioUsuarios, FxTasks tareasFx) {
        this.servicioUsuarios = servicioUsuarios;
        this.tareasFx = tareasFx;
    }

    public Parent vista() {
        tablaUsuarios = new TableView<>();
        Controles.columna(tablaUsuarios, "Nombre", Usuario::getNombre);
        Controles.columna(tablaUsuarios, "Usuario", Usuario::getUsername);
        Controles.columna(tablaUsuarios, "Rol", Usuario::getRol);
        crearFormulario();
        panelPagina = Controles.pagina("Empleados", "Cree cuentas o seleccione un empleado para cambiar su contraseña o desactivarlo.",
                tablaUsuarios, formulario, Controles.fila(Controles.principal("Crear empleado", this::crear),
                        Controles.boton("Cambiar contraseña", this::cambiarClave),
                        Controles.boton("Limpiar campos", this::limpiarCampos),
                        Controles.boton("Desactivar seleccionado", this::desactivar),
                        Controles.boton("Actualizar lista", this::cargar)));
        javafx.application.Platform.runLater(this::cargar);
        return panelPagina;
    }

    private void limpiarCampos() {
        tablaUsuarios.getSelectionModel().clearSelection();
        if (formulario != null) {
            formulario.limpiar();
        }
        if (comboRol != null) {
            comboRol.setValue(Rol.CAJERO);
        }
    }


    private void crearFormulario() {
        formulario = new Formulario();
        formulario.texto("Nombre");
        formulario.texto("Usuario");
        formulario.password("Contraseña");
        comboRol = formulario.opciones("Rol");
        comboRol.getItems().setAll(Rol.values());
        comboRol.setValue(Rol.CAJERO);
    }

    private void cargar() {
        tareasFx.ejecutar(panelPagina, servicioUsuarios::listar, lista -> {
            tablaUsuarios.getItems().setAll(lista);
            tablaUsuarios.refresh();
        });
    }

    private void crear() {
        String nombre = formulario.valor("Nombre");
        String nombreUsuario = formulario.valor("Usuario");
        String clave = formulario.valor("Contraseña");
        Rol rolElegido = comboRol.getValue();
        tareasFx.ejecutar(panelPagina, () -> servicioUsuarios.registrar(nombre, nombreUsuario, clave, rolElegido), usuario -> {
            formulario.limpiar();
            cargar();
        });
    }

    private void cambiarClave() {
        Usuario usuario = Controles.seleccionado(tablaUsuarios);
        String clave = formulario.valor("Contraseña");
        if (Dialogos.confirmar("¿Cambiar la contraseña de " + usuario.getUsername() + " por la ingresada?")) {
            tareasFx.ejecutar(panelPagina, () -> { servicioUsuarios.cambiarPassword(usuario.getId(), clave); return true; }, ok -> {
                formulario.poner("Contraseña", "");
                Dialogos.informar("Contraseña actualizada.");
            });
        }
    }

    private void desactivar() {
        Usuario usuario = Controles.seleccionado(tablaUsuarios);
        if (Dialogos.confirmar("¿Desactivar la cuenta " + usuario.getUsername() + "?")) {
            tareasFx.ejecutar(panelPagina, () -> { servicioUsuarios.desactivar(usuario.getId()); return true; }, ok -> cargar());
        }
    }
}

