package jass.com.agua.service.impl;

import jass.com.agua.entity.Periodo;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.PeriodoRepository;
import jass.com.agua.service.PeriodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PeriodoServiceImpl implements PeriodoService {

    private final PeriodoRepository periodoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Periodo> listarTodos() {
        return periodoRepository.findAllByOrderByAnioDescMesDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Periodo obtenerPorId(Long id) {
        return periodoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Periodo obtenerPorAnioYMes(Integer anio, Integer mes) {
        return periodoRepository.findByAnioAndMes(anio, mes)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado para " + mes + "/" + anio));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Periodo> listarPorEstado(String estado) {
        return periodoRepository.findByEstadoIgnoreCaseOrderByAnioDescMesDesc(estado);
    }

    @Override
    public Periodo registrar(Periodo periodo) {
        validar(periodo);
        if (periodoRepository.existsByAnioAndMes(periodo.getAnio(), periodo.getMes()))
            throw new BusinessException("Ya existe el periodo " + periodo.getMes() + "/" + periodo.getAnio());

        YearMonth ym = YearMonth.of(periodo.getAnio(), periodo.getMes());
        periodo.setIdPeriodo(null);
        if (periodo.getFechaInicio() == null) periodo.setFechaInicio(ym.atDay(1));
        if (periodo.getFechaFin() == null) periodo.setFechaFin(ym.atEndOfMonth());
        if (periodo.getEstado() == null || periodo.getEstado().isBlank()) periodo.setEstado("ABIERTO");
        return periodoRepository.save(periodo);
    }

    @Override
    public Periodo actualizar(Long id, Periodo datos) {
        Periodo actual = obtenerPorId(id);
        validar(datos);

        periodoRepository.findByAnioAndMes(datos.getAnio(), datos.getMes())
                .filter(p -> !p.getIdPeriodo().equals(id))
                .ifPresent(p -> { throw new BusinessException("Ya existe ese año y mes."); });

        actual.setAnio(datos.getAnio());
        actual.setMes(datos.getMes());
        actual.setFechaInicio(datos.getFechaInicio());
        actual.setFechaFin(datos.getFechaFin());
        if (datos.getEstado() != null) actual.setEstado(datos.getEstado());
        return periodoRepository.save(actual);
    }

    @Override
    public Periodo abrirPeriodo(Long id) {
        Periodo p = obtenerPorId(id);
        p.setEstado("ABIERTO");
        return periodoRepository.save(p);
    }

    @Override
    public Periodo cerrarPeriodo(Long id) {
        Periodo p = obtenerPorId(id);
        p.setEstado("CERRADO");
        return periodoRepository.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePeriodo(Integer anio, Integer mes) {
        return periodoRepository.existsByAnioAndMes(anio, mes);
    }

    private void validar(Periodo periodo) {
        if (periodo == null || periodo.getAnio() == null || periodo.getMes() == null)
            throw new BusinessException("Año y mes son obligatorios.");
        if (periodo.getMes() < 1 || periodo.getMes() > 12)
            throw new BusinessException("El mes debe estar entre 1 y 12.");
    }

    @Override
    public Periodo obtenerOCrearPeriodoActual() {

        YearMonth actual = YearMonth.now();

        int anio = actual.getYear();
        int mes = actual.getMonthValue();

        return periodoRepository
                .findByAnioAndMes(anio, mes)
                .orElseGet(() -> {

                    Periodo nuevo = Periodo.builder()
                            .anio(anio)
                            .mes(mes)
                            .fechaInicio(actual.atDay(1))
                            .fechaFin(actual.atEndOfMonth())
                            .estado("ABIERTO")
                            .build();

                    return periodoRepository.save(nuevo);
                });
    }
}
