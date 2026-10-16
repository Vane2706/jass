package jass.com.agua.repository;

import jass.com.agua.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByMaterial_IdMaterialOrderByFechaMovimientoDesc(Long idMaterial);
}