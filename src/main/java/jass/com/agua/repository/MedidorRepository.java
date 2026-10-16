package jass.com.agua.repository;

import jass.com.agua.entity.Medidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedidorRepository extends JpaRepository<Medidor, Long> {
    Optional<Medidor> findByNumeroMedidor(String numeroMedidor);

    boolean existsByNumeroMedidor(String numeroMedidor);

    List<Medidor> findByServicio_IdServicioOrderByFechaInstalacionDesc(Long idServicio);

    List<Medidor> findByEstadoIgnoreCaseOrderByNumeroMedidorAsc(String estado);

    Optional<Medidor> findFirstByServicio_IdServicioAndEstadoIgnoreCaseOrderByFechaInstalacionDesc(Long idServicio, String estado);

    boolean existsByServicio_IdServicioAndEstadoIgnoreCase(
            Long idServicio,
            String estado
    );

    @Query("""
       SELECT DISTINCT m
       FROM Medidor m
       JOIN FETCH m.servicio s
       JOIN FETCH s.inmueble i
       LEFT JOIN Titularidad t
              ON t.inmueble.idInmueble = i.idInmueble
             AND UPPER(t.estado) = 'ACTIVO'
       LEFT JOIN t.asociado a
       WHERE UPPER(m.estado) = 'ACTIVO'
         AND UPPER(s.estado) = 'ACTIVO'
         AND (
                LOWER(COALESCE(i.direccion, '')) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(i.referencia, '')) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(i.sector, '')) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(i.codigoInmueble, '')) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(m.numeroMedidor, '')) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(s.codigoServicio, '')) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(a.codigoAsociado, '')) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(a.nombres, '')) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(a.apellidoPaterno, '')) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(a.apellidoMaterno, '')) LIKE LOWER(CONCAT('%', :q, '%'))
         )
       ORDER BY i.direccion ASC
       """)
    List<Medidor> buscarParaLectura(@Param("q") String q);


    @Query("""
       SELECT DISTINCT m
       FROM Medidor m
       JOIN FETCH m.servicio s
       JOIN FETCH s.inmueble i
       WHERE UPPER(COALESCE(m.estado, '')) = 'ACTIVO'
         AND UPPER(COALESCE(s.estado, '')) = 'ACTIVO'
         AND EXISTS (
             SELECT ao.idAsignacion FROM AsignacionOperador ao
             WHERE ao.servicio.idServicio = s.idServicio
               AND ao.usuario.idUsuario = :idUsuario
               AND UPPER(ao.estado) = 'ACTIVO'
               AND (ao.fechaAsignacion IS NULL OR ao.fechaAsignacion <= :fecha)
               AND (ao.fechaFin IS NULL OR ao.fechaFin >= :fecha)
         )
         AND (
             :q = ''
             OR LOWER(COALESCE(m.numeroMedidor, '')) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(s.codigoServicio, '')) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(i.codigoInmueble, '')) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(i.direccion, '')) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(i.referencia, '')) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(i.sector, '')) LIKE LOWER(CONCAT('%', :q, '%'))
             OR EXISTS (
                 SELECT ti.idTitularidad FROM Titularidad ti JOIN ti.asociado a
                 WHERE ti.inmueble.idInmueble = i.idInmueble AND UPPER(ti.estado) = 'ACTIVO'
                   AND (LOWER(COALESCE(a.nombres, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                     OR LOWER(COALESCE(a.apellidoPaterno, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                     OR LOWER(COALESCE(a.apellidoMaterno, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                     OR LOWER(COALESCE(a.codigoAsociado, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                     OR LOWER(COALESCE(a.documento, '')) LIKE LOWER(CONCAT('%', :q, '%')))
             )
         )
       ORDER BY i.direccion ASC
       """)
    List<Medidor> buscarMedidoresAsignados(@Param("idUsuario") Long idUsuario,
                                           @Param("fecha") LocalDate fecha,
                                           @Param("q") String q);

}
