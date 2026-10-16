package jass.com.agua.service.impl;

import jass.com.agua.entity.Tarifa;
import jass.com.agua.entity.TipoServicio;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.TarifaRepository;
import jass.com.agua.repository.TipoServicioRepository;
import jass.com.agua.service.TarifaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TarifaServiceImpl implements TarifaService {

    private final TarifaRepository tarifaRepository;
    private final TipoServicioRepository tipoServicioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Tarifa> listarTodas() { return tarifaRepository.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public Tarifa obtenerPorId(Long id) {
        return tarifaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarifa no encontrada con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarifa> listarPorTipoServicio(Long idTipoServicio) {
        return tarifaRepository.findByTipoServicio_IdTipoServicioOrderByFechaInicioDesc(idTipoServicio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarifa> listarActivasPorTipoServicio(Long idTipoServicio) {
        return tarifaRepository.findByTipoServicio_IdTipoServicioAndEstadoIgnoreCaseOrderByFechaInicioDesc(idTipoServicio, "ACTIVO");
    }

    @Override
    @Transactional(readOnly = true)
    public Tarifa obtenerTarifaAplicable(Long idTipoServicio, BigDecimal consumo, LocalDate fecha) {
        if (consumo == null || consumo.compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessException("El consumo debe ser mayor o igual a cero.");
        return tarifaRepository.buscarTarifaAplicable(idTipoServicio, consumo, fecha != null ? fecha : LocalDate.now())
                .orElseThrow(() -> new BusinessException("No existe una tarifa activa aplicable al consumo indicado."));
    }

    @Override
    public Tarifa registrar(Tarifa tarifa) {
        validar(tarifa);
        TipoServicio tipo = tipoServicioRepository.findById(tarifa.getTipoServicio().getIdTipoServicio())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de servicio no encontrado."));

        tarifa.setIdTarifa(null);
        tarifa.setTipoServicio(tipo);
        if (tarifa.getEstado() == null || tarifa.getEstado().isBlank()) tarifa.setEstado("ACTIVO");
        return tarifaRepository.save(tarifa);
    }

    @Override
    public Tarifa actualizar(Long id, Tarifa datos) {
        Tarifa actual = obtenerPorId(id);
        validar(datos);

        TipoServicio tipo = tipoServicioRepository.findById(datos.getTipoServicio().getIdTipoServicio())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de servicio no encontrado."));

        actual.setTipoServicio(tipo);
        actual.setNombre(datos.getNombre());
        actual.setConsumoMinimo(datos.getConsumoMinimo());
        actual.setConsumoMaximo(datos.getConsumoMaximo());
        actual.setImporteFijo(datos.getImporteFijo());
        actual.setPrecioM3(datos.getPrecioM3());
        actual.setFechaInicio(datos.getFechaInicio());
        actual.setFechaFin(datos.getFechaFin());
        if (datos.getEstado() != null) actual.setEstado(datos.getEstado());
        return tarifaRepository.save(actual);
    }

    @Override
    public Tarifa cambiarEstado(Long id, String estado) {
        Tarifa tarifa = obtenerPorId(id);
        if (estado == null || estado.isBlank()) throw new BusinessException("El estado es obligatorio.");
        tarifa.setEstado(estado.trim().toUpperCase());
        return tarifaRepository.save(tarifa);
    }

    private void validar(Tarifa tarifa) {
        if (tarifa == null) throw new BusinessException("La tarifa es obligatoria.");
        if (tarifa.getTipoServicio() == null || tarifa.getTipoServicio().getIdTipoServicio() == null)
            throw new BusinessException("Debe indicar el tipo de servicio.");
        if (tarifa.getConsumoMinimo() != null && tarifa.getConsumoMaximo() != null
                && tarifa.getConsumoMinimo().compareTo(tarifa.getConsumoMaximo()) > 0)
            throw new BusinessException("El consumo mínimo no puede ser mayor al consumo máximo.");
        if (tarifa.getFechaInicio() != null && tarifa.getFechaFin() != null
                && tarifa.getFechaInicio().isAfter(tarifa.getFechaFin()))
            throw new BusinessException("La fecha de inicio no puede ser posterior a la fecha de fin.");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return false;
        }

        return tarifaRepository.existsByNombreIgnoreCase(
                nombre.trim()
        );
    }
}
