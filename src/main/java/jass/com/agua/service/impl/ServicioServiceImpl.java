package jass.com.agua.service.impl;

import jass.com.agua.entity.Inmueble;
import jass.com.agua.entity.Servicio;
import jass.com.agua.entity.TipoServicio;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.InmuebleRepository;
import jass.com.agua.repository.ServicioRepository;
import jass.com.agua.repository.TipoServicioRepository;
import jass.com.agua.service.AsignacionOperadorService;
import jass.com.agua.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;
    private final InmuebleRepository inmuebleRepository;
    private final TipoServicioRepository tipoServicioRepository;
    private final AsignacionOperadorService asignacionOperadorService;

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> listarTodos() { return servicioRepository.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public Page<Servicio> listarPaginado(
            String texto,
            int pagina,
            int tamanio
    ) {

        int paginaSegura =
                Math.max(pagina, 0);

        Pageable pageable =
                PageRequest.of(
                        paginaSegura,
                        tamanio,

                        // Los nuevos aparecen arriba
                        Sort.by(
                                Sort.Order.desc("fechaAlta"),
                                Sort.Order.desc("idServicio")
                        )
                );


        // Sin búsqueda
        if (texto == null
                || texto.isBlank()) {

            return servicioRepository
                    .findAll(pageable);
        }


        // Con búsqueda
        return servicioRepository
                .buscarPaginado(
                        texto.trim(),
                        pageable
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Servicio obtenerPorId(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Servicio obtenerPorCodigo(String codigoServicio) {
        return servicioRepository.findByCodigoServicio(codigoServicio)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con código: " + codigoServicio));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> listarPorInmueble(Long idInmueble) {
        return servicioRepository.findByInmueble_IdInmuebleOrderByFechaAltaDesc(idInmueble);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> listarPorTipoServicio(Long idTipoServicio) {
        return servicioRepository.findByTipoServicio_IdTipoServicioOrderByCodigoServicioAsc(idTipoServicio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> listarPorEstado(String estado) {
        return servicioRepository.findByEstadoIgnoreCaseOrderByCodigoServicioAsc(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public Servicio obtenerServicioActivoPorInmueble(Long idInmueble) {
        return servicioRepository.findFirstByInmueble_IdInmuebleAndEstadoIgnoreCaseOrderByFechaAltaDesc(idInmueble, "ACTIVO")
                .orElseThrow(() -> new ResourceNotFoundException("No existe servicio activo para el inmueble: " + idInmueble));
    }


    @Override
    public Servicio registrar(Servicio servicio) {

        if (servicio == null
                || servicio.getCodigoServicio() == null
                || servicio.getCodigoServicio().isBlank()) {
            throw new BusinessException(
                    "El código del servicio es obligatorio."
            );
        }

        if (servicio.getInmueble() == null
                || servicio.getInmueble().getIdInmueble() == null) {
            throw new BusinessException("Debe indicar el inmueble.");
        }

        if (servicio.getTipoServicio() == null
                || servicio.getTipoServicio().getIdTipoServicio() == null) {
            throw new BusinessException("Debe indicar el tipo de servicio.");
        }

        if (servicioRepository.existsByCodigoServicio(
                servicio.getCodigoServicio())) {
            throw new BusinessException(
                    "Ya existe un servicio con ese código."
            );
        }

        Inmueble inmueble = inmuebleRepository.findById(
                servicio.getInmueble().getIdInmueble()
        ).orElseThrow(() ->
                new ResourceNotFoundException("Inmueble no encontrado.")
        );

        TipoServicio tipo = tipoServicioRepository.findById(
                servicio.getTipoServicio().getIdTipoServicio()
        ).orElseThrow(() ->
                new ResourceNotFoundException("Tipo de servicio no encontrado.")
        );

        servicio.setIdServicio(null);
        servicio.setInmueble(inmueble);
        servicio.setTipoServicio(tipo);

        if (servicio.getFechaAlta() == null) {
            servicio.setFechaAlta(LocalDate.now());
        }

        if (servicio.getEstado() == null
                || servicio.getEstado().isBlank()) {
            servicio.setEstado("ACTIVO");
        }

        Servicio guardado = servicioRepository.save(servicio);

        if ("ACTIVO".equalsIgnoreCase(guardado.getEstado())) {
            asignacionOperadorService.sincronizarAsignacionesAutomaticas();
        }

        return guardado;
    }

    @Override
    public Servicio actualizar(Long id, Servicio datos) {
        Servicio actual = obtenerPorId(id);

        servicioRepository.findByCodigoServicio(datos.getCodigoServicio())
                .filter(s -> !s.getIdServicio().equals(id))
                .ifPresent(s -> { throw new BusinessException("El código del servicio ya está registrado."); });

        if (datos.getInmueble() != null && datos.getInmueble().getIdInmueble() != null) {
            actual.setInmueble(inmuebleRepository.findById(datos.getInmueble().getIdInmueble())
                    .orElseThrow(() -> new ResourceNotFoundException("Inmueble no encontrado.")));
        }
        if (datos.getTipoServicio() != null && datos.getTipoServicio().getIdTipoServicio() != null) {
            actual.setTipoServicio(tipoServicioRepository.findById(datos.getTipoServicio().getIdTipoServicio())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de servicio no encontrado.")));
        }

        actual.setCodigoServicio(datos.getCodigoServicio());
        actual.setFechaAlta(datos.getFechaAlta());
        actual.setObservaciones(datos.getObservaciones());
        if (datos.getEstado() != null) actual.setEstado(datos.getEstado());
        return servicioRepository.save(actual);
    }

    @Override
    public Servicio cambiarEstado(Long id, String estado) {
        Servicio servicio = obtenerPorId(id);
        if (estado == null || estado.isBlank()) throw new BusinessException("El estado es obligatorio.");
        servicio.setEstado(estado.trim().toUpperCase());
        return servicioRepository.save(servicio);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCodigo(String codigoServicio) {
        return servicioRepository.existsByCodigoServicio(codigoServicio);
    }
}
