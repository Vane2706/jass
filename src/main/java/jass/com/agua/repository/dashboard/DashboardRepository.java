package jass.com.agua.repository.dashboard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class DashboardRepository {

    @PersistenceContext
    private EntityManager entityManager;


    // =====================================================
    // TOTAL FACTURADO
    // =====================================================

    public BigDecimal obtenerTotalFacturado(
            Integer anio,
            Integer mes
    ) {

        String jpql = """
                SELECT COALESCE(SUM(f.total), 0)
                FROM Factura f
                WHERE f.periodo.anio = :anio
                  AND (:mes IS NULL OR f.periodo.mes = :mes)
                """;

        return entityManager
                .createQuery(jpql, BigDecimal.class)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getSingleResult();
    }


    // =====================================================
    // CONSUMO TOTAL
    // =====================================================

    public BigDecimal obtenerConsumoTotal(
            Integer anio,
            Integer mes
    ) {

        String jpql = """
                SELECT COALESCE(SUM(l.consumo), 0)
                FROM Lectura l
                WHERE l.periodo.anio = :anio
                  AND (:mes IS NULL OR l.periodo.mes = :mes)
                """;

        return entityManager
                .createQuery(jpql, BigDecimal.class)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getSingleResult();
    }


    // =====================================================
    // TOTAL RECAUDADO
    // =====================================================

    public BigDecimal obtenerTotalRecaudado(
            Integer anio,
            Integer mes
    ) {

        String jpql = """
                SELECT COALESCE(SUM(p.montoTotal), 0)
                FROM Pago p
                WHERE p.estado = 'APLICADO'
                  AND YEAR(p.fechaPago) = :anio
                  AND (:mes IS NULL OR MONTH(p.fechaPago) = :mes)
                """;

        return entityManager
                .createQuery(jpql, BigDecimal.class)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getSingleResult();
    }


    // =====================================================
    // MOROSIDAD
    // =====================================================

    public BigDecimal obtenerMorosidad(
            Integer anio,
            Integer mes
    ) {

        String jpql = """
                SELECT COALESCE(
                    SUM(
                        f.total -
                        COALESCE(
                            (
                                SELECT SUM(dp.montoAplicado)
                                FROM DetallePago dp
                                WHERE dp.factura.idFactura = f.idFactura
                            ),
                            0
                        )
                    ),
                    0
                )
                FROM Factura f
                WHERE f.periodo.anio = :anio
                  AND (:mes IS NULL OR f.periodo.mes = :mes)
                  AND UPPER(f.estado) IN ('PENDIENTE', 'PARCIAL')
                """;

        return entityManager
                .createQuery(jpql, BigDecimal.class)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getSingleResult();
    }


    // =====================================================
    // SERVICIOS APTOS PARA CORTE
    //
    // Regla:
    // mínimo 2 periodos pendientes/parciales.
    // =====================================================

    public Long contarServiciosAptosCorte(
            Integer anio,
            Integer mes
    ) {

        String jpql = """
                SELECT COUNT(DISTINCT f.servicio.idServicio)
                FROM Factura f
                WHERE f.periodo.anio = :anio
                  AND (:mes IS NULL OR f.periodo.mes <= :mes)
                  AND UPPER(f.estado) IN ('PENDIENTE', 'PARCIAL')
                GROUP BY f.servicio.idServicio
                HAVING COUNT(DISTINCT f.periodo.idPeriodo) >= 2
                """;

        List<Long> resultados =
                entityManager
                        .createQuery(jpql, Long.class)
                        .setParameter("anio", anio)
                        .setParameter("mes", mes)
                        .getResultList();

        return (long) resultados.size();
    }


    // =====================================================
    // FACTURACIÓN POR MES
    // =====================================================

    public List<Object[]> obtenerFacturacionMensual(
            Integer anio
    ) {

        String jpql = """
                SELECT
                    f.periodo.mes,
                    COALESCE(SUM(f.total), 0)
                FROM Factura f
                WHERE f.periodo.anio = :anio
                GROUP BY f.periodo.mes
                ORDER BY f.periodo.mes
                """;

        return entityManager
                .createQuery(jpql, Object[].class)
                .setParameter("anio", anio)
                .getResultList();
    }


    // =====================================================
    // RECAUDACIÓN POR MES
    // =====================================================

    public List<Object[]> obtenerRecaudacionMensual(
            Integer anio
    ) {

        String jpql = """
                SELECT
                    MONTH(p.fechaPago),
                    COALESCE(SUM(p.montoTotal), 0)
                FROM Pago p
                WHERE YEAR(p.fechaPago) = :anio
                  AND UPPER(p.estado) = 'APLICADO'
                GROUP BY MONTH(p.fechaPago)
                ORDER BY MONTH(p.fechaPago)
                """;

        return entityManager
                .createQuery(jpql, Object[].class)
                .setParameter("anio", anio)
                .getResultList();
    }


    // =====================================================
    // CONSUMO POR MES
    // =====================================================

    public List<Object[]> obtenerConsumoMensual(
            Integer anio
    ) {

        String jpql = """
                SELECT
                    l.periodo.mes,
                    COALESCE(SUM(l.consumo), 0)
                FROM Lectura l
                WHERE l.periodo.anio = :anio
                GROUP BY l.periodo.mes
                ORDER BY l.periodo.mes
                """;

        return entityManager
                .createQuery(jpql, Object[].class)
                .setParameter("anio", anio)
                .getResultList();
    }


    // =====================================================
    // CONSUMO POR TIPO DE SERVICIO
    // =====================================================

    public List<Object[]> obtenerConsumoPorTipo(
            Integer anio,
            Integer mes
    ) {

        String jpql = """
                SELECT
                    l.medidor.servicio.tipoServicio.nombre,
                    COALESCE(SUM(l.consumo), 0)
                FROM Lectura l
                WHERE l.periodo.anio = :anio
                  AND (:mes IS NULL OR l.periodo.mes = :mes)
                GROUP BY l.medidor.servicio.tipoServicio.nombre
                ORDER BY SUM(l.consumo) DESC
                """;

        return entityManager
                .createQuery(jpql, Object[].class)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getResultList();
    }


    // =====================================================
    // MOROSIDAD POR SECTOR
    // =====================================================

    public List<Object[]> obtenerMorosidadPorSector(
            Integer anio,
            Integer mes
    ) {

        String jpql = """
                SELECT
                    COALESCE(f.servicio.inmueble.sector, 'Sin sector'),
                    COALESCE(
                        SUM(
                            f.total -
                            COALESCE(
                                (
                                    SELECT SUM(dp.montoAplicado)
                                    FROM DetallePago dp
                                    WHERE dp.factura.idFactura = f.idFactura
                                ),
                                0
                            )
                        ),
                        0
                    )
                FROM Factura f
                WHERE f.periodo.anio = :anio
                  AND (:mes IS NULL OR f.periodo.mes = :mes)
                  AND UPPER(f.estado) IN ('PENDIENTE', 'PARCIAL')
                GROUP BY f.servicio.inmueble.sector
                ORDER BY 2 DESC
                """;

        return entityManager
                .createQuery(jpql, Object[].class)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getResultList();
    }


    // =====================================================
    // RECAUDACIÓN POR METODO DE PAGO
    // =====================================================

    public List<Object[]> obtenerRecaudacionPorMetodo(
            Integer anio,
            Integer mes
    ) {

        String jpql = """
                SELECT
                    UPPER(p.metodoPago),
                    COALESCE(SUM(p.montoTotal), 0)
                FROM Pago p
                WHERE YEAR(p.fechaPago) = :anio
                  AND (:mes IS NULL OR MONTH(p.fechaPago) = :mes)
                  AND UPPER(p.estado) = 'APLICADO'
                GROUP BY UPPER(p.metodoPago)
                ORDER BY SUM(p.montoTotal) DESC
                """;

        return entityManager
                .createQuery(jpql, Object[].class)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getResultList();
    }
}