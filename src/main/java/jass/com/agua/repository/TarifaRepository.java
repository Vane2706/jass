package jass.com.agua.repository;

import jass.com.agua.entity.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
    List<Tarifa> findByTipoServicio_IdTipoServicioOrderByFechaInicioDesc(Long idTipoServicio);

    List<Tarifa> findByTipoServicio_IdTipoServicioAndEstadoIgnoreCaseOrderByFechaInicioDesc(Long idTipoServicio, String estado);

    @Query("""
            SELECT t FROM Tarifa t
            WHERE t.tipoServicio.idTipoServicio = :idTipoServicio
              AND LOWER(t.estado) = LOWER(:estado)
              AND (t.fechaInicio IS NULL OR t.fechaInicio <= :fecha)
              AND (t.fechaFin IS NULL OR t.fechaFin >= :fecha)
              AND (t.consumoMinimo IS NULL OR t.consumoMinimo <= :consumo)
              AND (t.consumoMaximo IS NULL OR t.consumoMaximo >= :consumo)
            ORDER BY t.fechaInicio DESC
            """)
    List<Tarifa> buscarTarifasAplicables(@Param("idTipoServicio") Long idTipoServicio, @Param("consumo") BigDecimal consumo, @Param("fecha") LocalDate fecha, @Param("estado") String estado);

    default Optional<Tarifa> buscarTarifaAplicable(Long idTipoServicio, BigDecimal consumo, LocalDate fecha) {
        return buscarTarifasAplicables(idTipoServicio, consumo, fecha, "ACTIVO").stream().findFirst();
    }

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<Tarifa> findByNombreIgnoreCase(String nombre);
}
