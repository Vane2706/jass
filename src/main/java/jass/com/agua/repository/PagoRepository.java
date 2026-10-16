package jass.com.agua.repository;

import jass.com.agua.entity.Pago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByAsociado_IdAsociadoOrderByFechaPagoDesc(Long idAsociado);

    List<Pago> findByUsuario_IdUsuarioOrderByFechaPagoDesc(Long idUsuario);

    List<Pago> findByEstadoIgnoreCaseOrderByFechaPagoDesc(String estado);

    List<Pago> findByFechaPagoBetweenOrderByFechaPagoDesc(LocalDateTime desde, LocalDateTime hasta);

    List<Pago> findByAsociado_IdAsociadoAndEstadoIgnoreCaseOrderByFechaPagoDesc(Long idAsociado, String estado);

    Page<Pago> findByAsociado_IdAsociado(
            Long idAsociado,
            Pageable pageable
    );
}
