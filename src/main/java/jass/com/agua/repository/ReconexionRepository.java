package jass.com.agua.repository;

import jass.com.agua.entity.Reconexion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReconexionRepository extends JpaRepository<Reconexion, Long> {
    List<Reconexion> findBySuspension_OrdenCorte_Servicio_IdServicioOrderByFechaSolicitudDesc(Long idServicio);

    @Query("""
        SELECT COUNT(r)
        FROM Reconexion r

        WHERE r.fechaReconexion IS NOT NULL

          AND YEAR(r.fechaReconexion) = :anio

          AND (
                :mes IS NULL
                OR MONTH(r.fechaReconexion) = :mes
          )
    """)
    Long contarReconexionesDashboard(
            @Param("anio") Integer anio,
            @Param("mes") Integer mes
    );

    Optional<Reconexion>
    findFirstBySuspension_IdSuspensionOrderByIdReconexionDesc(
            Long idSuspension
    );
}