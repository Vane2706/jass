package jass.com.agua.repository;

import jass.com.agua.entity.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
    List<Comprobante> findByRequerimiento_IdRequerimientoOrderByFechaEmisionDesc(Long idRequerimiento);
}