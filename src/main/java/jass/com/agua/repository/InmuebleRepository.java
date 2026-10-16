package jass.com.agua.repository;

import jass.com.agua.entity.Inmueble;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InmuebleRepository extends JpaRepository<Inmueble, Long> {
    Optional<Inmueble> findByCodigoInmueble(String codigoInmueble);

    boolean existsByCodigoInmueble(String codigoInmueble);

    List<Inmueble> findByEstadoIgnoreCaseOrderByDireccionAsc(String estado);

    List<Inmueble> findBySectorIgnoreCaseOrderByDireccionAsc(String sector);

    @Query("""
            SELECT i FROM Inmueble i
            WHERE LOWER(i.codigoInmueble) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(i.direccion) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(i.referencia) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(i.sector) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(i.manzana) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(i.numeroLote) LIKE LOWER(CONCAT('%', :texto, '%'))
            ORDER BY i.direccion
            """)
    List<Inmueble> buscar(@Param("texto") String texto);


    @Query("""
    SELECT i
    FROM Inmueble i
    WHERE
           LOWER(COALESCE(i.codigoInmueble, ''))
               LIKE LOWER(CONCAT('%', :texto, '%'))

        OR LOWER(COALESCE(i.direccion, ''))
               LIKE LOWER(CONCAT('%', :texto, '%'))

        OR LOWER(COALESCE(i.referencia, ''))
               LIKE LOWER(CONCAT('%', :texto, '%'))

        OR LOWER(COALESCE(i.sector, ''))
               LIKE LOWER(CONCAT('%', :texto, '%'))

        OR LOWER(COALESCE(i.numeroLote, ''))
               LIKE LOWER(CONCAT('%', :texto, '%'))

        OR LOWER(COALESCE(i.manzana, ''))
               LIKE LOWER(CONCAT('%', :texto, '%'))

        OR LOWER(COALESCE(i.distrito, ''))
               LIKE LOWER(CONCAT('%', :texto, '%'))

        OR EXISTS (
            SELECT t.idTitularidad
            FROM Titularidad t
            JOIN t.asociado a
            WHERE t.inmueble.idInmueble = i.idInmueble
              AND UPPER(t.estado) = 'ACTIVO'
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
    Page<Inmueble> buscarPaginado(
            @Param("texto") String texto,
            Pageable pageable
    );

    @Query("""
        SELECT i
        FROM Inmueble i
        WHERE UPPER(i.estado) = 'ACTIVO'
          AND (
                LOWER(COALESCE(i.codigoInmueble, ''))
                    LIKE LOWER(CONCAT('%', :q, '%'))

             OR LOWER(COALESCE(i.direccion, ''))
                    LIKE LOWER(CONCAT('%', :q, '%'))

             OR LOWER(COALESCE(i.referencia, ''))
                    LIKE LOWER(CONCAT('%', :q, '%'))

             OR LOWER(COALESCE(i.sector, ''))
                    LIKE LOWER(CONCAT('%', :q, '%'))

             OR LOWER(COALESCE(i.distrito, ''))
                    LIKE LOWER(CONCAT('%', :q, '%'))

             OR LOWER(COALESCE(i.numeroLote, ''))
                    LIKE LOWER(CONCAT('%', :q, '%'))

             OR LOWER(COALESCE(i.manzana, ''))
                    LIKE LOWER(CONCAT('%', :q, '%'))
          )
        ORDER BY i.direccion ASC
        """)
    List<Inmueble> buscarActivosParaServicio(
            @Param("q") String q,
            Pageable pageable
    );

    @Query("""
    SELECT i
    FROM Inmueble i
    WHERE LOWER(COALESCE(i.direccion, ''))
          LIKE LOWER(CONCAT('%', :q, '%'))
    ORDER BY i.direccion ASC
    """)
    List<Inmueble> buscarDirecciones(
            @Param("q") String q,
            Pageable pageable
    );
}
