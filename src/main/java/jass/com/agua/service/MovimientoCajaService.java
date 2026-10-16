package jass.com.agua.service;

import jass.com.agua.entity.MovimientoCaja;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface MovimientoCajaService {
    List<MovimientoCaja> listarTodos();

    Page<MovimientoCaja> listarPorCajaPaginado(
            Long idCaja,
            int pagina,
            int tamanio
    );

    MovimientoCaja obtenerPorId(Long id);

    List<MovimientoCaja> listarPorCaja(Long idCaja);

    MovimientoCaja registrar(Long idCaja, Long idUsuario, Long idPago, String tipo, String concepto, BigDecimal monto, String observaciones);
}