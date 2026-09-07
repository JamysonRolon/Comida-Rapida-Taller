package com.comidasrapidas.repository;

import com.comidasrapidas.model.Cliente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByNumeroDocumento(String documento);
    List<Cliente> findAllByOrderByNombreAsc();
    List<Cliente> findByActivoTrueOrderByNombreAsc();
}
