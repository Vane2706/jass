package jass.com.agua.repository;

import jass.com.agua.entity.DetalleRequerimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleRequerimientoRepository extends JpaRepository<DetalleRequerimiento, Long> {
    List<DetalleRequerimiento> findByRequerimiento_IdRequerimientoOrderByIdDetalleRequerimientoAsc(Long idRequerimiento);
}