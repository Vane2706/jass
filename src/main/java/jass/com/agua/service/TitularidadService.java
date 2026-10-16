package jass.com.agua.service;

import jass.com.agua.entity.Asociado;
import jass.com.agua.entity.Titularidad;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TitularidadService {
    List<Titularidad> listarTodas();

    Titularidad obtenerPorId(Long id);

    List<Titularidad> listarPorAsociado(Long idAsociado);

    List<Titularidad> listarPorInmueble(Long idInmueble);

    Titularidad obtenerTitularidadVigentePorInmueble(Long idInmueble);

    Titularidad registrar(Long idAsociado, Long idInmueble, LocalDate fechaInicio, String motivo, String observaciones);

    Titularidad finalizarTitularidad(Long idTitularidad, LocalDate fechaFin, String motivo, String observaciones);

    Titularidad cambiarTitular(Long idInmueble, Long idNuevoAsociado, LocalDate fechaCambio, String motivo, String observaciones);

    Map<Long, Asociado> obtenerTitularesActivosPorInmuebles(
            List<Long> idsInmuebles
    );
}
