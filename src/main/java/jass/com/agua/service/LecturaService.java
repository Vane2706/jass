package jass.com.agua.service;

import jass.com.agua.entity.Lectura;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface LecturaService {
    List<Lectura> listarTodas();
    Page<Lectura> listarPaginado(
            Long idPeriodo,
            Long idInmueble,
            int pagina,
            int tamanio
    );
    Lectura obtenerPorId(Long id);
    List<Lectura> listarPorMedidor(Long idMedidor);
    List<Lectura> listarPorPeriodo(Long idPeriodo);
    List<Lectura> listarPorOperadorYPeriodo(Long idUsuario, Long idPeriodo);
    List<Lectura> listarPorEstadoSincronizacion(String estadoSincronizacion);
    Lectura obtenerUltimaLecturaPorMedidor(Long idMedidor);
    Lectura registrarLectura(
            Long idUsuario,
            Long idMedidor,
            BigDecimal lecturaActual,
            String observaciones,
            String origen
    );
    Lectura actualizarObservaciones(Long idLectura, String observaciones);
    Lectura marcarComoSincronizada(Long idLectura);
    boolean existeLecturaParaPeriodo(Long idMedidor, Long idPeriodo);
    BigDecimal calcularConsumo(Long idMedidor, BigDecimal lecturaActual);
}
