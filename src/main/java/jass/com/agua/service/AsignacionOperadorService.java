package jass.com.agua.service;

import jass.com.agua.entity.AsignacionOperador;
import java.time.LocalDate;
import java.util.List;

public interface AsignacionOperadorService {
    List<AsignacionOperador> listarTodas();
    AsignacionOperador obtenerPorId(Long id);
    List<AsignacionOperador> listarPorOperador(Long idUsuario);
    List<AsignacionOperador> listarPorServicio(Long idServicio);
    List<AsignacionOperador> listarAsignacionesVigentes(Long idUsuario, LocalDate fecha);
    AsignacionOperador asignar(Long idUsuario, Long idServicio, LocalDate fechaAsignacion, LocalDate fechaFin);
    AsignacionOperador finalizarAsignacion(Long idAsignacion, LocalDate fechaFin);
    boolean existeAsignacionActiva(Long idUsuario, Long idServicio);
    void sincronizarAsignacionesAutomaticas();
}
