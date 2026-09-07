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
    private final UsuarioService usuarios;
    private final FxTasks tareas;
    private TableView<Usuario> tabla;
    private Formulario form;
    private ComboBox<Rol> rol;
    private VBox pagina;

    public UsuarioController(UsuarioService usuarios, FxTasks tareas) {
        this.usuarios = usuarios;
        this.tareas = tareas;
    }

    public Parent vista() {
        tabla = new TableView<>();
        Controles.columna(tabla, "Nombre", Usuario::getNombre);
        Controles.columna(tabla, "Usuario", Usuario::getUsername);
        Controles.columna(tabla, "Rol", Usuario::getRol);
        Controles.columna(tabla, "Estado", u -> Formato.estado(u.isActivo()));
        crearFormulario();
        pagina = Controles.pagina("Empleados", "Cree cuentas o seleccione un empleado para cambiar su contraseña o desactivarlo.",
                tabla, form, Controles.fila(Controles.principal("Crear empleado", this::crear),
                        Controles.boton("Cambiar contraseña", this::password),
                        Controles.boton("Desactivar seleccionado", this::desactivar),
                        Controles.boton("Actualizar lista", this::cargar)));
        javafx.application.Platform.runLater(this::cargar);
        return pagina;
    }

    private void crearFormulario() {
        form = new Formulario();
        form.texto("Nombre");
        form.texto("Usuario");
        form.password("Contraseña");
        rol = form.opciones("Rol");
        rol.getItems().setAll(Rol.values());
        rol.setValue(Rol.CAJERO);
    }

    private void cargar() {
        tareas.ejecutar(pagina, usuarios::listar, lista -> tabla.getItems().setAll(lista));
    }

    private void crear() {
        String nombre = form.valor("Nombre");
        String username = form.valor("Usuario");
        String password = form.valor("Contraseña");
        Rol elegido = rol.getValue();
        tareas.ejecutar(pagina, () -> usuarios.registrar(nombre, username, password, elegido), usuario -> {
            form.limpiar();
            cargar();
        });
    }

    private void password() {
        Usuario usuario = Controles.seleccionado(tabla);
        String password = form.valor("Contraseña");
        if (Dialogos.confirmar("¿Cambiar la contraseña de " + usuario.getUsername() + " por la ingresada?")) {
            tareas.ejecutar(pagina, () -> { usuarios.cambiarPassword(usuario.getId(), password); return true; }, ok -> {
                form.poner("Contraseña", "");
                Dialogos.informar("Contraseña actualizada.");
            });
        }
    }

    private void desactivar() {
        Usuario usuario = Controles.seleccionado(tabla);
        if (Dialogos.confirmar("¿Desactivar la cuenta " + usuario.getUsername() + "?")) {
            tareas.ejecutar(pagina, () -> { usuarios.desactivar(usuario.getId()); return true; }, ok -> cargar());
        }
    }
}
