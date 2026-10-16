package jass.com.agua.repository;

import jass.com.agua.entity.Factura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
    Optional<Factura> findByNumeroFactura(String numeroFactura);

    boolean existsByNumeroFactura(String numeroFactura);

    boolean existsByServicio_IdServicioAndPeriodo_IdPeriodo(Long idServicio, Long idPeriodo);

    Optional<Factura> findFirstByServicio_IdServicioAndPeriodo_IdPeriodoOrderByIdFacturaDesc(Long idServicio, Long idPeriodo);

    List<Factura> findByServicio_IdServicioOrderByFechaEmisionDesc(Long idServicio);

    Page<Factura> findByServicio_IdServicio(
            Long idServicio,
            Pageable pageable
    );

    List<Factura> findByPeriodo_IdPeriodoOrderByNumeroFacturaAsc(Long idPeriodo);

    List<Factura> findByEstadoIgnoreCaseOrderByFechaVencimientoAsc(String estado);

    List<Factura> findByServicio_IdServicioAndEstadoInOrderByFechaVencimientoAsc(Long idServicio, List<String> estados);

    List<Factura>
    findByEstadoInOrderByPeriodo_AnioDescPeriodo_MesDesc(
            Collection<String> estados
    );

    @EntityGraph(
            attributePaths = {
                    "servicio",
                    "servicio.inmueble",
                    "periodo"
            }
    )
    Page<Factura> findByPeriodo_IdPeriodo(
            Long idPeriodo,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(f) FROM Factura f
            WHERE f.servicio.idServicio = :idServicio
              AND LOWER(f.estado) = LOWER(:estado)
            """)
    long contarPorServicioYEstado(@Param("idServicio") Long idServicio, @Param("estado") String estado);

    Page<Factura> findByEstadoIn(
            Collection<String> estados,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "servicio",
                    "servicio.inmueble",
                    "periodo"
            }
    )
    Page<Factura> findAll(Pageable pageable);


    @Query("""
    SELECT f
    FROM Factura f
    JOIN FETCH f.servicio s
    JOIN FETCH s.inmueble i
    JOIN FETCH f.periodo p
    WHERE (:idPeriodo IS NULL OR p.idPeriodo = :idPeriodo)
      AND (
            :direccion IS NULL
            OR :direccion = ''
            OR LOWER(i.direccion)
                LIKE LOWER(CONCAT('%', :direccion, '%'))
          )
    """)
    Page<Factura> buscarPaginado(
            @Param("idPeriodo") Long idPeriodo,
            @Param("direccion") String direccion,
            Pageable pageable
    );
}
