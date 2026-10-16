package jass.com.agua.repository.dashboard;

import jass.com.agua.entity.Lectura;
import jass.com.agua.entity.Suspension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DashRepo extends JpaRepository<Suspension, Long> {
    // ======================================================
// SERVICIOS CON PERIODOS VENCIDOS
// ======================================================

    @Query("""
    SELECT
        s.idServicio,
        s.codigoServicio,
        CONCAT(
            COALESCE(a.nombres, ''),
            ' ',
            COALESCE(a.apellidoPaterno, ''),
            ' ',
            COALESCE(a.apellidoMaterno, '')
        ),
        COALESCE(i.sector, 'Sin sector'),
        COUNT(DISTINCT f.periodo.idPeriodo),
        COALESCE(SUM(f.total), 0)
    FROM Factura f
    JOIN f.servicio s
    JOIN s.inmueble i

    LEFT JOIN Titularidad t
        ON t.inmueble.idInmueble = i.idInmueble
        AND UPPER(t.estado) = 'ACTIVO'

    LEFT JOIN t.asociado a

    WHERE UPPER(f.estado) IN ('PENDIENTE', 'PARCIAL')

      AND f.periodo.anio = :anio

      AND (
            :mes IS NULL
            OR f.periodo.mes <= :mes
      )

    GROUP BY
        s.idServicio,
        s.codigoServicio,
        a.nombres,
        a.apellidoPaterno,
        a.apellidoMaterno,
        i.sector

    HAVING COUNT(DISTINCT f.periodo.idPeriodo) >= 1

    ORDER BY
        COUNT(DISTINCT f.periodo.idPeriodo) DESC,
        COALESCE(SUM(f.total), 0) DESC
""")
    List<Object[]> obtenerServiciosVencidos(
            @Param("anio") Integer anio,
            @Param("mes") Integer mes
    );


// ======================================================
// LECTURAS DEL PERIODO PARA ANÁLISIS
// ======================================================

    @Query("""
    SELECT l
    FROM Lectura l
    JOIN FETCH l.medidor m
    JOIN FETCH m.servicio s
    JOIN FETCH s.inmueble i
    JOIN FETCH l.periodo p

    WHERE p.anio = :anio

      AND (
            :mes IS NULL
            OR p.mes = :mes
      )

    ORDER BY
        l.fechaLectura DESC,
        l.idLectura DESC
""")
    List<Lectura> obtenerLecturasDashboard(
            @Param("anio") Integer anio,
            @Param("mes") Integer mes
    );


// ======================================================
// PROMEDIO HISTÓRICO ANTERIOR A UNA LECTURA
// ======================================================

    @Query("""
    SELECT AVG(l.consumo)
    FROM Lectura l

    WHERE l.medidor.idMedidor = :idMedidor

      AND l.idLectura <> :idLectura

      AND (
            l.periodo.anio < :anio

            OR (
                l.periodo.anio = :anio
                AND l.periodo.mes < :mes
            )
      )
""")
    Double obtenerPromedioHistorico(
            @Param("idMedidor") Long idMedidor,
            @Param("idLectura") Long idLectura,
            @Param("anio") Integer anio,
            @Param("mes") Integer mes
    );
}
