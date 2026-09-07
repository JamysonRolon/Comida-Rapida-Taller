package com.comidasrapidas.repository;

import com.comidasrapidas.model.DetalleVenta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
}
