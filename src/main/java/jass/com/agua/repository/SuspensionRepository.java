package jass.com.agua.repository;

import jass.com.agua.entity.Suspension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SuspensionRepository extends JpaRepository<Suspension, Long> {
    List<Suspension> findByOrdenCorte_Servicio_IdServicioOrderByFechaSuspensionDesc(Long idServicio);

    Optional<Suspension> findFirstByOrdenCorte_IdOrdenCorteOrderByFechaSuspensionDesc(
            Long idOrdenCorte
    );

    Optional<Suspension> findFirstByOrdenCorte_Servicio_IdServicioAndEstadoIgnoreCaseOrderByFechaSuspensionDesc(Long idServicio, String estado);

    @Query("""
        SELECT COUNT(s)
        FROM Suspension s

        WHERE YEAR(s.fechaSuspension) = :anio

          AND (
                :mes IS NULL
                OR MONTH(s.fechaSuspension) = :mes
          )
    """)
    Long contarCortesEjecutadosDashboard(
            @Param("anio") Integer anio,
            @Param("mes") Integer mes
    );
}