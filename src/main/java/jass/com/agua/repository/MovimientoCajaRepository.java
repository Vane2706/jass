package jass.com.agua.repository;

import jass.com.agua.entity.MovimientoCaja;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {
    List<MovimientoCaja> findByCaja_IdCajaOrderByFechaMovimientoDesc(Long idCaja);

    List<MovimientoCaja> findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(LocalDateTime desde, LocalDateTime hasta);

    @Query("select coalesce(sum(m.monto),0) from MovimientoCaja m where m.caja.idCaja=:caja and upper(m.tipoMovimiento)=upper(:tipo)")
    BigDecimal sumarPorCajaYTipo(@Param("caja") Long caja, @Param("tipo") String tipo);

    boolean existsByPago_IdPago(
            Long idPago
    );

    Page<MovimientoCaja>
    findByCaja_IdCaja(
            Long idCaja,
            Pageable pageable
    );
}