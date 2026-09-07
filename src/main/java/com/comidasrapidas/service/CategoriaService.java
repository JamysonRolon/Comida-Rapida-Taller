package com.comidasrapidas.service;

import com.comidasrapidas.exception.DatosInvalidosException;
import com.comidasrapidas.model.Categoria;
import com.comidasrapidas.repository.CategoriaRepository;
import com.comidasrapidas.repository.ProductoRepository;
import com.comidasrapidas.util.Validacion;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {
    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    private final SesionService sesion;

    public CategoriaService(CategoriaRepository categorias, ProductoRepository productos, SesionService sesion) {
        this.categorias = categorias;
        this.productos = productos;
        this.sesion = sesion;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar(boolean soloActivas) {
        sesion.exigirUsuario();
        return soloActivas ? categorias.findByActivoTrueOrderByNombreAsc() : categorias.findAllByOrderByNombreAsc();
    }

    @Transactional
    public Categoria registrar(String nombre) {
        sesion.exigirAdministrador();
        return categorias.save(new Categoria(validar(nombre, null)));
    }

    @Transactional
    public Categoria actualizar(Long id, String nombre) {
        sesion.exigirAdministrador();
        Categoria categoria = bloquear(id);
        categoria.renombrar(validar(nombre, id));
        return categoria;
    }

    @Transactional
    public void desactivar(Long id) {
        sesion.exigirAdministrador();
        Categoria categoria = bloquear(id);
        Validacion.exigir(!productos.existsByCategoriaIdAndActivoTrue(id),
                "La categoría contiene productos activos. Desactívelos o cambie su categoría primero.");
        categoria.desactivar();
    }

    private Categoria bloquear(Long id) {
        Validacion.exigir(id != null, "Seleccione una categoría.");
        return categorias.bloquear(id).orElseThrow(() -> new DatosInvalidosException("La categoría no existe."));
    }

    private String validar(String nombre, Long id) {
        String limpio = Validacion.texto(nombre, "Nombre de categoría", 80);
        boolean duplicado = categorias.findByNombreIgnoreCase(limpio)
                .filter(c -> !Objects.equals(c.getId(), id)).isPresent();
        Validacion.exigir(!duplicado, "Ya existe una categoría con ese nombre.");
        return limpio;
    }
}
