package jass.com.agua.repository;

import jass.com.agua.entity.Asociado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AsociadoRepository extends JpaRepository<Asociado, Long> {
    Optional<Asociado> findByCodigoAsociado(String codigoAsociado);

    Optional<Asociado> findByDocumento(String documento);

    boolean existsByCodigoAsociado(String codigoAsociado);

    boolean existsByDocumento(String documento);

    List<Asociado> findByEstadoIgnoreCaseOrderByApellidoPaternoAscNombresAsc(String estado);

    @Query("""
            SELECT a FROM Asociado a
            WHERE LOWER(a.codigoAsociado) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(a.documento) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(a.nombres) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(a.apellidoPaterno) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(a.apellidoMaterno) LIKE LOWER(CONCAT('%', :texto, '%'))
            ORDER BY a.apellidoPaterno, a.apellidoMaterno, a.nombres
            """)
    List<Asociado> buscar(@Param("texto") String texto);

    @Query("""
            SELECT a
            FROM Asociado a
            WHERE LOWER(a.codigoAsociado)
                    LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(COALESCE(a.nombres, ''))
                    LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(COALESCE(a.apellidoPaterno, ''))
                    LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(COALESCE(a.apellidoMaterno, ''))
                    LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(COALESCE(a.documento, ''))
                    LIKE LOWER(CONCAT('%', :texto, '%'))
            """)
    Page<Asociado> buscarPaginado(
            @Param("texto") String texto,
            Pageable pageable
    );

    @Query("""
        SELECT a
        FROM Asociado a
        WHERE UPPER(a.estado) = 'ACTIVO'
          AND (
                LOWER(COALESCE(a.codigoAsociado, ''))
                    LIKE LOWER(CONCAT('%', :q, '%'))

             OR LOWER(COALESCE(a.documento, ''))
                    LIKE LOWER(CONCAT('%', :q, '%'))

             OR LOWER(COALESCE(a.nombres, ''))
                    LIKE LOWER(CONCAT('%', :q, '%'))

             OR LOWER(COALESCE(a.apellidoPaterno, ''))
                    LIKE LOWER(CONCAT('%', :q, '%'))

             OR LOWER(COALESCE(a.apellidoMaterno, ''))
                    LIKE LOWER(CONCAT('%', :q, '%'))

             OR LOWER(
                    CONCAT(
                        COALESCE(a.nombres, ''),
                        ' ',
                        COALESCE(a.apellidoPaterno, ''),
                        ' ',
                        COALESCE(a.apellidoMaterno, '')
                    )
                )
                    LIKE LOWER(CONCAT('%', :q, '%'))
          )
        ORDER BY
            a.apellidoPaterno ASC,
            a.apellidoMaterno ASC,
            a.nombres ASC
        """)
    List<Asociado> buscarActivosParaTitular(
            @Param("q") String q,
            Pageable pageable
    );
}
