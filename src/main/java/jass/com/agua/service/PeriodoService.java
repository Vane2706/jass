package jass.com.agua.service;

import jass.com.agua.entity.Periodo;
import java.util.List;

public interface PeriodoService {
    List<Periodo> listarTodos();
    Periodo obtenerPorId(Long id);
    Periodo obtenerPorAnioYMes(Integer anio, Integer mes);
    List<Periodo> listarPorEstado(String estado);
    Periodo registrar(Periodo periodo);
    Periodo actualizar(Long id, Periodo periodo);
    Periodo abrirPeriodo(Long id);
    Periodo cerrarPeriodo(Long id);
    boolean existePeriodo(Integer anio, Integer mes);
    Periodo obtenerOCrearPeriodoActual();
}
