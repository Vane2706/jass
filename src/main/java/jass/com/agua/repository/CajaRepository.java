package jass.com.agua.repository;

import jass.com.agua.entity.Caja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CajaRepository extends JpaRepository<Caja, Long> {
    List<Caja> findByEstadoIgnoreCaseOrderByFechaAperturaDesc(String estado);

    Optional<Caja> findFirstByUsuario_IdUsuarioAndEstadoIgnoreCaseOrderByFechaAperturaDesc(Long idUsuario, String estado);
    boolean existsByEstadoIgnoreCase(
            String estado
    );

    Optional<Caja>
    findFirstByEstadoIgnoreCaseOrderByFechaAperturaDescIdCajaDesc(
            String estado
    );
}