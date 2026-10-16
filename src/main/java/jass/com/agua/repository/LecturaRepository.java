package jass.com.agua.repository;

import jass.com.agua.entity.Lectura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LecturaRepository extends JpaRepository<Lectura, Long> {
    boolean existsByMedidor_IdMedidorAndPeriodo_IdPeriodo(Long idMedidor, Long idPeriodo);

    Optional<Lectura> findByIdCapturaMovil(String idCapturaMovil);

    Optional<Lectura> findFirstByMedidor_IdMedidorAndPeriodo_IdPeriodoOrderByIdLecturaDesc(Long idMedidor, Long idPeriodo);

    Optional<Lectura> findFirstByMedidor_IdMedidorOrderByFechaLecturaDesc(Long idMedidor);

    List<Lectura> findByMedidor_IdMedidorOrderByFechaLecturaDesc(Long idMedidor);

    List<Lectura> findByPeriodo_IdPeriodoOrderByFechaLecturaAsc(Long idPeriodo);

    List<Lectura> findByUsuario_IdUsuarioAndPeriodo_IdPeriodoOrderByFechaLecturaAsc(Long idUsuario, Long idPeriodo);

    List<Lectura> findByEstadoSincronizacionIgnoreCaseOrderByFechaLecturaAsc(String estadoSincronizacion);

    Optional<Lectura>
    findFirstByMedidor_Servicio_IdServicioAndPeriodo_IdPeriodoOrderByIdLecturaDesc(
            Long idServicio,
            Long idPeriodo
    );

    @EntityGraph(
            attributePaths = {
                    "medidor",
                    "medidor.servicio",
                    "medidor.servicio.inmueble",
                    "periodo"
            }
    )
    Page<Lectura> findByPeriodo_IdPeriodo(
            Long idPeriodo,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "medidor",
                    "medidor.servicio",
                    "medidor.servicio.inmueble",
                    "periodo"
            }
    )
    Page<Lectura> findAll(Pageable pageable);

    @EntityGraph(
            attributePaths = {
                    "medidor",
                    "medidor.servicio",
                    "medidor.servicio.inmueble",
                    "periodo"
            }
    )
    @Query("""
    SELECT l
    FROM Lectura l
    JOIN l.medidor m
    JOIN m.servicio s
    JOIN s.inmueble i
    WHERE (:idPeriodo IS NULL OR l.periodo.idPeriodo = :idPeriodo)
      AND (:idInmueble IS NULL OR i.idInmueble = :idInmueble)
    """)
    Page<Lectura> buscarPaginado(
            @Param("idPeriodo") Long idPeriodo,
            @Param("idInmueble") Long idInmueble,
            Pageable pageable
    );


    List<Lectura> findByMedidor_IdMedidorInAndPeriodo_IdPeriodo(List<Long> idsMedidores, Long idPeriodo);

    @Query("""
        SELECT l FROM Lectura l
        WHERE l.medidor.idMedidor IN :idsMedidores
          AND l.periodo.idPeriodo <> :idPeriodo
          AND l.idLectura = (SELECT MAX(l2.idLectura) FROM Lectura l2
              WHERE l2.medidor.idMedidor = l.medidor.idMedidor
                AND l2.periodo.idPeriodo <> :idPeriodo)
        ORDER BY l.medidor.idMedidor
        """)
    List<Lectura> buscarUltimasLecturasPrevias(@Param("idsMedidores") List<Long> idsMedidores,
                                               @Param("idPeriodo") Long idPeriodo);

    @Query("""
        SELECT l FROM Lectura l
        WHERE l.medidor.idMedidor IN :idsMedidores
          AND l.idLectura = (SELECT MAX(l2.idLectura) FROM Lectura l2
              WHERE l2.medidor.idMedidor = l.medidor.idMedidor)
        ORDER BY l.medidor.idMedidor
        """)
    List<Lectura> buscarUltimasLecturasPorMedidores(@Param("idsMedidores") List<Long> idsMedidores);

    Optional<Lectura> findFirstByMedidor_IdMedidorAndFechaLecturaBeforeOrderByFechaLecturaDescIdLecturaDesc(
            Long idMedidor, java.time.LocalDateTime fechaLectura);

}
