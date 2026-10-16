package jass.com.agua.service;

import jass.com.agua.entity.Tarifa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TarifaService {
    List<Tarifa> listarTodas();

    Tarifa obtenerPorId(Long id);

    List<Tarifa> listarPorTipoServicio(Long idTipoServicio);

    List<Tarifa> listarActivasPorTipoServicio(Long idTipoServicio);

    Tarifa obtenerTarifaAplicable(Long idTipoServicio, BigDecimal consumo, LocalDate fecha);

    Tarifa registrar(Tarifa tarifa);

    Tarifa actualizar(Long id, Tarifa tarifa);

    Tarifa cambiarEstado(Long id, String estado);
    boolean existeNombre(String nombre);
}
