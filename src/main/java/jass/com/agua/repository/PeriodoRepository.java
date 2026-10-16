package jass.com.agua.repository;

import jass.com.agua.entity.Periodo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PeriodoRepository extends JpaRepository<Periodo, Long> {
    Optional<Periodo> findByAnioAndMes(Integer anio, Integer mes);

    boolean existsByAnioAndMes(Integer anio, Integer mes);

    List<Periodo> findByEstadoIgnoreCaseOrderByAnioDescMesDesc(String estado);

    List<Periodo> findAllByOrderByAnioDescMesDesc();
}
