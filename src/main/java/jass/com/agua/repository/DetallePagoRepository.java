package jass.com.agua.repository;

import jass.com.agua.entity.DetallePago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface DetallePagoRepository extends JpaRepository<DetallePago, Long> {
    List<DetallePago> findByPago_IdPagoOrderByIdDetallePagoAsc(Long idPago);

    List<DetallePago> findByFactura_IdFacturaOrderByIdDetallePagoAsc(Long idFactura);

    boolean existsByPago_IdPagoAndFactura_IdFactura(Long idPago, Long idFactura);

    @Query("""
            SELECT COALESCE(SUM(dp.montoAplicado), 0)
            FROM DetallePago dp
            WHERE dp.factura.idFactura = :idFactura
            """)
    BigDecimal sumarMontoAplicadoPorFactura(@Param("idFactura") Long idFactura);
}
