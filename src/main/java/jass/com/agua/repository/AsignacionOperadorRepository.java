package jass.com.agua.repository;

import jass.com.agua.entity.AsignacionOperador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AsignacionOperadorRepository extends JpaRepository<AsignacionOperador, Long> {
    List<AsignacionOperador> findByUsuario_IdUsuarioAndEstadoIgnoreCaseOrderByFechaAsignacionDesc(Long idUsuario, String estado);

    List<AsignacionOperador> findByServicio_IdServicioOrderByFechaAsignacionDesc(Long idServicio);

    boolean existsByUsuario_IdUsuarioAndServicio_IdServicioAndEstadoIgnoreCase(Long idUsuario, Long idServicio, String estado);

    @Query("""
            SELECT a FROM AsignacionOperador a
            WHERE a.usuario.idUsuario = :idUsuario
              AND LOWER(a.estado) = LOWER(:estado)
              AND (a.fechaAsignacion IS NULL OR a.fechaAsignacion <= :fecha)
              AND (a.fechaFin IS NULL OR a.fechaFin >= :fecha)
            ORDER BY a.servicio.codigoServicio
            """)
    List<AsignacionOperador> buscarAsignacionesVigentes(@Param("idUsuario") Long idUsuario, @Param("fecha") LocalDate fecha, @Param("estado") String estado);


    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM AsignacionOperador a
        WHERE a.usuario.idUsuario = :idUsuario
          AND a.servicio.idServicio = :idServicio
          AND UPPER(a.estado) = 'ACTIVO'
          AND (a.fechaAsignacion IS NULL OR a.fechaAsignacion <= :fecha)
          AND (a.fechaFin IS NULL OR a.fechaFin >= :fecha)
        """)
    boolean existeAsignacionVigente(@Param("idUsuario") Long idUsuario,
                                    @Param("idServicio") Long idServicio,
                                    @Param("fecha") LocalDate fecha);

}