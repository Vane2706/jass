package jass.com.agua.repository;

import jass.com.agua.entity.Requerimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequerimientoRepository extends JpaRepository<Requerimiento, Long> {
    List<Requerimiento> findByProveedor_IdProveedorOrderByFechaRequerimientoDesc(Long idProveedor);

    List<Requerimiento> findByEstadoIgnoreCaseOrderByFechaRequerimientoDesc(String estado);
}