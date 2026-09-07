package com.comidasrapidas.service;

import com.comidasrapidas.model.Categoria;
import com.comidasrapidas.model.Cliente;
import com.comidasrapidas.model.Producto;
import com.comidasrapidas.model.Rol;
import com.comidasrapidas.model.Usuario;
import com.comidasrapidas.model.Venta;
import com.comidasrapidas.repository.CategoriaRepository;
import com.comidasrapidas.repository.ClienteRepository;
import com.comidasrapidas.repository.ProductoRepository;
import com.comidasrapidas.repository.UsuarioRepository;
import com.comidasrapidas.repository.VentaRepository;
import com.comidasrapidas.util.Validacion;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoDataService {
    public static final String USUARIO_DANIEL = "daniel";
    public static final String USUARIO_JUAN = "juan";

    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    private final ClienteRepository clientes;
    private final VentaRepository ventas;
    private final BCryptPasswordEncoder encoder;
    private final Clock clock;

    public DemoDataService(UsuarioRepository usuarios, CategoriaRepository categorias,
                           ProductoRepository productos, ClienteRepository clientes,
                           VentaRepository ventas, BCryptPasswordEncoder encoder, Clock clock) {
        this.usuarios = usuarios;
        this.categorias = categorias;
        this.productos = productos;
        this.clientes = clientes;
        this.ventas = ventas;
        this.encoder = encoder;
        this.clock = clock;
    }

    @Transactional
    public DemoDataSummary cargar(String claveDaniel, String claveJuan) {
        Validacion.password(claveDaniel);
        Validacion.password(claveJuan);
        Map<String, Usuario> empleados = crearEmpleados(claveDaniel, claveJuan);
        Map<String, Categoria> grupos = crearCategorias();
        Map<String, Producto> catalogo = crearProductos(grupos);
        Map<String, Cliente> personas = crearClientes();
        int ventasCreadas = crearVentas(empleados, catalogo, personas);
        return new DemoDataSummary(empleados.size(), grupos.size(), catalogo.size(),
                personas.size(), ventasCreadas);
    }

    private Map<String, Usuario> crearEmpleados(String claveDaniel, String claveJuan) {
        Map<String, Usuario> resultado = new LinkedHashMap<>();
        resultado.put(USUARIO_DANIEL, usuario("Daniel", USUARIO_DANIEL, claveDaniel, Rol.CAJERO));
        resultado.put(USUARIO_JUAN, usuario("Juan", USUARIO_JUAN, claveJuan, Rol.ADMINISTRADOR));
        return resultado;
    }

    private Usuario usuario(String nombre, String username, String clave, Rol rol) {
        return usuarios.findByUsername(username).orElseGet(() ->
                usuarios.save(new Usuario(nombre, username, encoder.encode(clave), rol)));
    }

    private Map<String, Categoria> crearCategorias() {
        Map<String, Categoria> resultado = new LinkedHashMap<>();
        categoria(resultado, "Hamburguesas");
        categoria(resultado, "Perros calientes");
        categoria(resultado, "Salchipapas");
        categoria(resultado, "Combos");
        categoria(resultado, "Bebidas");
        return resultado;
    }

    private void categoria(Map<String, Categoria> destino, String nombre) {
        Categoria categoria = categorias.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> categorias.save(new Categoria(nombre)));
        destino.put(nombre, categoria);
    }

    private Map<String, Producto> crearProductos(Map<String, Categoria> grupos) {
        Map<String, Producto> resultado = new LinkedHashMap<>();
        producto(resultado, grupos, "Hamburguesa clásica", "Carne, queso y vegetales", "15000", 30, "Hamburguesas");
        producto(resultado, grupos, "Hamburguesa doble bacon", "Doble carne, queso y tocineta", "22000", 20, "Hamburguesas");
        producto(resultado, grupos, "Perro tradicional", "Salchicha, salsas, queso y papa", "12000", 25, "Perros calientes");
        producto(resultado, grupos, "Perro especial", "Salchicha, pollo, queso y papa", "17000", 18, "Perros calientes");
        producto(resultado, grupos, "Salchipapa personal", "Papa, salchicha y salsas", "14000", 25, "Salchipapas");
        producto(resultado, grupos, "Salchipapa especial", "Papa, salchicha, pollo y queso", "19000", 15, "Salchipapas");
        producto(resultado, grupos, "Combo clásico", "Hamburguesa clásica, papas y gaseosa", "22000", 20, "Combos");
        producto(resultado, grupos, "Combo familiar", "Cuatro hamburguesas, papas y gaseosa", "48000", 10, "Combos");
        producto(resultado, grupos, "Gaseosa 400 ml", "Bebida gaseosa personal", "4500", 60, "Bebidas");
        producto(resultado, grupos, "Limonada natural", "Limonada preparada al momento", "5500", 35, "Bebidas");
        producto(resultado, grupos, "Agua", "Botella de agua", "3500", 50, "Bebidas");
        producto(resultado, grupos, "Jugo natural", "Jugo de fruta en agua", "6000", 30, "Bebidas");
        return resultado;
    }

    private void producto(Map<String, Producto> destino, Map<String, Categoria> grupos,
                          String nombre, String descripcion, String precio, int stock, String grupo) {
        Producto producto = productos.findByNombreIgnoreCase(nombre).orElseGet(() -> productos.save(
                new Producto(nombre, descripcion, new BigDecimal(precio), stock, grupos.get(grupo))));
        destino.put(nombre, producto);
    }

    private Map<String, Cliente> crearClientes() {
        Map<String, Cliente> resultado = new LinkedHashMap<>();
        cliente(resultado, "1010000001", "Laura", "Gómez", "3005550101", "laura.gomez@example.com");
        cliente(resultado, "1010000002", "Santiago", "Torres", "3015550102", "santiago.torres@example.com");
        cliente(resultado, "1010000003", "Valentina", "Ruiz", "3025550103", "valentina.ruiz@example.com");
        cliente(resultado, "1010000004", "Carlos", "Mendoza", "3035550104", "carlos.mendoza@example.com");
        cliente(resultado, "1010000005", "Mariana", "Castro", "3045550105", "mariana.castro@example.com");
        return resultado;
    }

    private void cliente(Map<String, Cliente> destino, String documento, String nombre,
                         String apellido, String telefono, String correo) {
        Cliente cliente = clientes.findByNumeroDocumento(documento).orElseGet(() -> clientes.save(
                new Cliente("CC", documento, nombre, apellido, telefono, correo)));
        destino.put(documento, cliente);
    }

    private int crearVentas(Map<String, Usuario> empleados, Map<String, Producto> catalogo,
                            Map<String, Cliente> personas) {
        if (ventas.count() > 0) {
            return 0;
        }
        venta(Duration.ofHours(2), personas.get("1010000001"), empleados.get(USUARIO_DANIEL),
                catalogo.get("Hamburguesa clásica"), 2, catalogo.get("Gaseosa 400 ml"), 2);
        venta(Duration.ofHours(5), null, empleados.get(USUARIO_DANIEL),
                catalogo.get("Perro especial"), 1, catalogo.get("Limonada natural"), 1);
        venta(Duration.ofDays(1), personas.get("1010000003"), empleados.get(USUARIO_JUAN),
                catalogo.get("Combo familiar"), 1, catalogo.get("Agua"), 2);
        return 3;
    }

    private void venta(Duration antiguedad, Cliente cliente, Usuario empleado,
                       Producto primero, int cantidadPrimero, Producto segundo, int cantidadSegundo) {
        Venta venta = new Venta(clock.instant().minus(antiguedad), cliente, empleado);
        primero.descontar(cantidadPrimero);
        venta.agregar(primero, cantidadPrimero);
        segundo.descontar(cantidadSegundo);
        venta.agregar(segundo, cantidadSegundo);
        ventas.save(venta);
    }
}
