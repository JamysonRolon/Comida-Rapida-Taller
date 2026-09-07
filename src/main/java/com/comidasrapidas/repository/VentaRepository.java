package com.comidasrapidas.repository;

import com.comidasrapidas.model.Venta;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    @EntityGraph(attributePaths = {"cliente", "usuario"})
    List<Venta> findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraDesc(
            Instant desde, Instant hasta);

    @EntityGraph(attributePaths = {"cliente", "usuario", "detalles", "detalles.producto"})
    @Query("select v from Venta v where v.id = :id")
    Optional<Venta> buscarCompleta(@Param("id") Long id);

    @Query("select coalesce(sum(v.total), 0) from Venta v where v.fechaHora >= :desde and v.fechaHora < :hasta")
    BigDecimal sumarPeriodo(@Param("desde") Instant desde, @Param("hasta") Instant hasta);
}
