package jass.com.agua.service;

import jass.com.agua.entity.Caja;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CajaService {
    List<Caja> listarTodos();

    BigDecimal obtenerSaldoInicialSiguienteCaja();

    Caja obtenerPorId(Long id);

    Caja abrir(Long idUsuario, String nombre, BigDecimal saldoInicial);

    Caja cerrar(Long idCaja, BigDecimal saldoReal);

    boolean existeCajaAbierta();

    Optional<Caja> obtenerCajaAbiertaUsuario(
            Long idUsuario
    );

    List<Caja> listarAbiertas();

    Page<Caja> listarPaginado(
            int pagina,
            int tamanio
    );

    BigDecimal calcularSaldoActual(
            Long idCaja
    );
}