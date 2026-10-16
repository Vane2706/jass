package jass.com.agua.repository;

import jass.com.agua.entity.Titularidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TitularidadRepository extends JpaRepository<Titularidad, Long> {
    List<Titularidad> findByAsociado_IdAsociadoOrderByFechaInicioDesc(Long idAsociado);

    List<Titularidad> findByInmueble_IdInmuebleOrderByFechaInicioDesc(Long idInmueble);

    Optional<Titularidad> findFirstByInmueble_IdInmuebleAndEstadoIgnoreCaseOrderByFechaInicioDesc(Long idInmueble, String estado);

    boolean existsByAsociado_IdAsociadoAndInmueble_IdInmuebleAndEstadoIgnoreCase(Long idAsociado, Long idInmueble, String estado);

    @Query("""
    SELECT t
    FROM Titularidad t
    JOIN FETCH t.asociado a
    JOIN FETCH t.inmueble i
    WHERE i.idInmueble IN :idsInmuebles
      AND UPPER(t.estado) = 'ACTIVO'
    ORDER BY t.fechaInicio DESC
    """)
    List<Titularidad> buscarTitularesActivosPorInmuebles(
            @Param("idsInmuebles") List<Long> idsInmuebles
    );
}
