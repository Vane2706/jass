package jass.com.agua.service.impl;

import jass.com.agua.entity.TipoServicio;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.TipoServicioRepository;
import jass.com.agua.service.TipoServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TipoServicioServiceImpl implements TipoServicioService {

    private final TipoServicioRepository tipoServicioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TipoServicio> listarTodos() { return tipoServicioRepository.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public TipoServicio obtenerPorId(Long id) {
        return tipoServicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de servicio no encontrado con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public TipoServicio obtenerPorNombre(String nombre) {
        return tipoServicioRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de servicio no encontrado: " + nombre));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoServicio> listarPorEstado(String estado) {
        return tipoServicioRepository.findByEstadoIgnoreCaseOrderByNombreAsc(estado);
    }

    @Override
    public TipoServicio registrar(TipoServicio tipoServicio) {
        validar(tipoServicio);
        if (tipoServicioRepository.existsByNombreIgnoreCase(tipoServicio.getNombre()))
            throw new BusinessException("Ya existe un tipo de servicio con ese nombre.");

        tipoServicio.setIdTipoServicio(null);
        tipoServicio.setEstado(tipoServicio.getEstado() == null || tipoServicio.getEstado().isBlank() ? "ACTIVO" : tipoServicio.getEstado().toUpperCase());
        return tipoServicioRepository.save(tipoServicio);
    }

    @Override
    public TipoServicio actualizar(Long id, TipoServicio datos) {
        TipoServicio actual = obtenerPorId(id);
        validar(datos);

        tipoServicioRepository.findByNombreIgnoreCase(datos.getNombre())
                .filter(t -> !t.getIdTipoServicio().equals(id))
                .ifPresent(t -> { throw new BusinessException("El nombre del tipo de servicio ya está registrado."); });

        actual.setNombre(datos.getNombre());
        actual.setDescripcion(datos.getDescripcion());
        if (datos.getEstado() != null) actual.setEstado(datos.getEstado());
        return tipoServicioRepository.save(actual);
    }

    @Override
    public TipoServicio cambiarEstado(Long id, String estado) {
        TipoServicio actual = obtenerPorId(id);
        if (estado == null || estado.isBlank()) throw new BusinessException("El estado es obligatorio.");
        actual.setEstado(estado.trim().toUpperCase());
        return tipoServicioRepository.save(actual);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeNombre(String nombre) {
        return tipoServicioRepository.existsByNombreIgnoreCase(nombre);
    }

    private void validar(TipoServicio tipoServicio) {
        if (tipoServicio == null || tipoServicio.getNombre() == null || tipoServicio.getNombre().isBlank())
            throw new BusinessException("El nombre del tipo de servicio es obligatorio.");
    }
}
