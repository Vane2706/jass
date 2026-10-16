package jass.com.agua.repository;

import jass.com.agua.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Material> findByEstadoIgnoreCaseOrderByNombreAsc(String estado);
}