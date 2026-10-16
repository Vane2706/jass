package jass.com.agua.repository;

import jass.com.agua.entity.CierreCaja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CierreCajaRepository extends JpaRepository<CierreCaja, Long> {
    List<CierreCaja> findByCaja_IdCajaOrderByFechaCierreDesc(Long idCaja);
}