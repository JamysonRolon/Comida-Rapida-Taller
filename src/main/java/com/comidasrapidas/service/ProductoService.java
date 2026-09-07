package com.comidasrapidas.service;

import com.comidasrapidas.exception.DatosInvalidosException;
import com.comidasrapidas.exception.ProductoNoEncontradoException;
import com.comidasrapidas.model.Categoria;
import com.comidasrapidas.model.Producto;
import com.comidasrapidas.repository.CategoriaRepository;
import com.comidasrapidas.repository.ProductoRepository;
import com.comidasrapidas.util.Validacion;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {
    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final com.comidasrapidas.repository.DetalleVentaRepository detallesVenta;
    private final SesionService sesion;

    public ProductoService(ProductoRepository productos, CategoriaRepository categorias,
                           com.comidasrapidas.repository.DetalleVentaRepository detallesVenta,
                           SesionService sesion) {
        this.productos = productos;
        this.categorias = categorias;
        this.detallesVenta = detallesVenta;
        this.sesion = sesion;
    }

    @Transactional(readOnly = true)
    public List<Producto> listar(boolean soloActivos) {
        sesion.exigirUsuario();
        return soloActivos ? productos.findByActivoTrueOrderByNombreAsc() : productos.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Producto consultar(Long id) {
        sesion.exigirUsuario();
        Validacion.exigir(id != null, "Seleccione un producto.");
        return productos.findById(id).orElseThrow(ProductoNoEncontradoException::new);
    }

    @Transactional
    public Producto registrar(Producto datos) {
        sesion.exigirAdministrador();
        return productos.save(validar(datos));
    }

    @Transactional
    public Producto actualizar(Long id, Producto datos) {
        sesion.exigirAdministrador();
        Producto existente = bloquear(id);
        Producto limpio = validar(datos);
        existente.actualizar(limpio, limpio.getCategoria());
        return existente;
    }

    @Transactional
    public Producto actualizar(Long id, Producto datos, int stockEsperado) {
        sesion.exigirAdministrador();
        Producto existente = bloquear(id);
        Validacion.exigir(existente.getStock() == stockEsperado,
                "El stock cambió mientras editaba. Actualice la lista y revise las existencias.");
        Producto limpio = validar(datos);
        existente.actualizar(limpio, limpio.getCategoria());
        return existente;
    }

    @Transactional
    public void eliminar(Long id) {
        sesion.exigirAdministrador();
        Producto existente = bloquear(id);
        if (detallesVenta.existsByProductoId(id)) {
            existente.desactivar();
            productos.save(existente);
            throw new DatosInvalidosException("No se puede borrar físicamente el producto '" + existente.getNombre() + "' porque tiene ventas registradas en el historial. Ha sido desactivado del menú.");
        }
        productos.delete(existente);
    }

    @Transactional
    public void desactivar(Long id) {
        sesion.exigirAdministrador();
        bloquear(id).desactivar();
    }

    private Producto bloquear(Long id) {
        Validacion.exigir(id != null, "Seleccione un producto.");
        return productos.bloquear(id).orElseThrow(ProductoNoEncontradoException::new);
    }

    private Producto validar(Producto datos) {
        Validacion.exigir(datos != null, "Ingrese los datos del producto.");
        Validacion.exigir(datos.getStock() >= 0, "El stock no puede ser negativo.");
        Validacion.exigir(datos.getCategoria() != null, "Seleccione una categoría.");
        Long categoriaId = datos.getCategoria().getId();
        Validacion.exigir(categoriaId != null, "Seleccione una categoría registrada.");
        Categoria categoria = categorias.bloquear(categoriaId)
                .orElseThrow(() -> new DatosInvalidosException("La categoría no existe."));
        Validacion.exigir(categoria.isActivo(), "La categoría está inactiva.");
        return new Producto(Validacion.texto(datos.getNombre(), "Nombre", 100),
                Validacion.opcional(datos.getDescripcion(), "Descripción", 500),
                Validacion.precio(datos.getPrecio()), datos.getStock(), categoria);
    }
}
