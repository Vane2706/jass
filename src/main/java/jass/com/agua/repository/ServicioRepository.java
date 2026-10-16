package jass.com.agua.repository;

import jass.com.agua.entity.Servicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    Optional<Servicio> findByCodigoServicio(String codigoServicio);

    boolean existsByCodigoServicio(String codigoServicio);

    List<Servicio> findByInmueble_IdInmuebleOrderByFechaAltaDesc(Long idInmueble);

    List<Servicio> findByTipoServicio_IdTipoServicioOrderByCodigoServicioAsc(Long idTipoServicio);

    List<Servicio> findByEstadoIgnoreCaseOrderByCodigoServicioAsc(String estado);

    Optional<Servicio> findFirstByInmueble_IdInmuebleAndEstadoIgnoreCaseOrderByFechaAltaDesc(Long idInmueble, String estado);

    @Query("""
        SELECT s
        FROM Servicio s
        JOIN s.inmueble i
        JOIN s.tipoServicio t
        WHERE
               LOWER(COALESCE(s.codigoServicio, ''))
                    LIKE LOWER(CONCAT('%', :texto, '%'))

            OR LOWER(COALESCE(i.codigoInmueble, ''))
                    LIKE LOWER(CONCAT('%', :texto, '%'))

            OR LOWER(COALESCE(i.direccion, ''))
                    LIKE LOWER(CONCAT('%', :texto, '%'))

            OR LOWER(COALESCE(i.sector, ''))
                    LIKE LOWER(CONCAT('%', :texto, '%'))

            OR LOWER(COALESCE(t.nombre, ''))
                    LIKE LOWER(CONCAT('%', :texto, '%'))

            OR LOWER(COALESCE(s.estado, ''))
                    LIKE LOWER(CONCAT('%', :texto, '%'))

            OR EXISTS (
                SELECT ti.idTitularidad
                FROM Titularidad ti
                JOIN ti.asociado a
                WHERE ti.inmueble.idInmueble = i.idInmueble
                  AND UPPER(ti.estado) = 'ACTIVO'
                  AND (
                         LOWER(COALESCE(a.nombres, ''))
                            LIKE LOWER(CONCAT('%', :texto, '%'))

                      OR LOWER(COALESCE(a.apellidoPaterno, ''))
                            LIKE LOWER(CONCAT('%', :texto, '%'))

                      OR LOWER(COALESCE(a.apellidoMaterno, ''))
                            LIKE LOWER(CONCAT('%', :texto, '%'))

                      OR LOWER(
                            CONCAT(
                                COALESCE(a.nombres, ''),
                                ' ',
                                COALESCE(a.apellidoPaterno, ''),
                                ' ',
                                COALESCE(a.apellidoMaterno, '')
                            )
                         ) LIKE LOWER(CONCAT('%', :texto, '%'))

                      OR LOWER(COALESCE(a.codigoAsociado, ''))
                            LIKE LOWER(CONCAT('%', :texto, '%'))

                      OR LOWER(COALESCE(a.documento, ''))
                            LIKE LOWER(CONCAT('%', :texto, '%'))
                  )
            )
        """)
    Page<Servicio> buscarPaginado(
            @Param("texto") String texto,
            Pageable pageable
    );
}
