package jass.com.agua.repository;

import jass.com.agua.entity.OrdenCorte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrdenCorteRepository extends JpaRepository<OrdenCorte, Long> {
    List<OrdenCorte> findByServicio_IdServicioOrderByFechaGeneracionDesc(Long idServicio);

    List<OrdenCorte> findByEstadoIgnoreCaseOrderByFechaGeneracionDesc(String estado);

    Optional<OrdenCorte> findFirstByServicio_IdServicioAndEstadoIgnoreCaseOrderByFechaGeneracionDesc(Long idServicio, String estado);

        @Query("""
        SELECT COUNT(o)
        FROM OrdenCorte o
        WHERE YEAR(o.fechaGeneracion) = :anio
          AND (
                :mes IS NULL
                OR MONTH(o.fechaGeneracion) = :mes
          )
    """)
        Long contarOrdenesDashboard(
                @Param("anio") Integer anio,
                @Param("mes") Integer mes
        );

}