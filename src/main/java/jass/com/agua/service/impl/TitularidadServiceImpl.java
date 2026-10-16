package jass.com.agua.service.impl;

import jass.com.agua.entity.Asociado;
import jass.com.agua.entity.Inmueble;
import jass.com.agua.entity.Titularidad;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.AsociadoRepository;
import jass.com.agua.repository.InmuebleRepository;
import jass.com.agua.repository.TitularidadRepository;
import jass.com.agua.service.TitularidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class TitularidadServiceImpl implements TitularidadService {

    private final TitularidadRepository titularidadRepository;
    private final AsociadoRepository asociadoRepository;
    private final InmuebleRepository inmuebleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Titularidad> listarTodas() { return titularidadRepository.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Asociado> obtenerTitularesActivosPorInmuebles(
            List<Long> idsInmuebles
    ) {

        if (idsInmuebles == null || idsInmuebles.isEmpty()) {
            return Map.of();
        }

        List<Titularidad> titularidades =
                titularidadRepository
                        .buscarTitularesActivosPorInmuebles(
                                idsInmuebles
                        );

        Map<Long, Asociado> resultado =
                new LinkedHashMap<>();

        for (Titularidad t : titularidades) {

            if (t.getInmueble() == null
                    || t.getInmueble().getIdInmueble() == null
                    || t.getAsociado() == null) {
                continue;
            }
            resultado.putIfAbsent(
                    t.getInmueble().getIdInmueble(),
                    t.getAsociado()
            );
        }

        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public Titularidad obtenerPorId(Long id) {
        return titularidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Titularidad no encontrada con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Titularidad> listarPorAsociado(Long idAsociado) {
        return titularidadRepository.findByAsociado_IdAsociadoOrderByFechaInicioDesc(idAsociado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Titularidad> listarPorInmueble(Long idInmueble) {
        return titularidadRepository.findByInmueble_IdInmuebleOrderByFechaInicioDesc(idInmueble);
    }

    @Override
    @Transactional(readOnly = true)
    public Titularidad obtenerTitularidadVigentePorInmueble(Long idInmueble) {
        return titularidadRepository
                .findFirstByInmueble_IdInmuebleAndEstadoIgnoreCaseOrderByFechaInicioDesc(idInmueble, "ACTIVO")
                .orElseThrow(() -> new ResourceNotFoundException("No existe titularidad activa para el inmueble: " + idInmueble));
    }

    @Override
    public Titularidad registrar(Long idAsociado, Long idInmueble, LocalDate fechaInicio, String motivo, String observaciones) {
        Asociado asociado = asociadoRepository.findById(idAsociado)
                .orElseThrow(() -> new ResourceNotFoundException("Asociado no encontrado: " + idAsociado));
        Inmueble inmueble = inmuebleRepository.findById(idInmueble)
                .orElseThrow(() -> new ResourceNotFoundException("Inmueble no encontrado: " + idInmueble));

        if (titularidadRepository.findFirstByInmueble_IdInmuebleAndEstadoIgnoreCaseOrderByFechaInicioDesc(idInmueble, "ACTIVO").isPresent())
            throw new BusinessException("El inmueble ya posee una titularidad activa.");

        Titularidad titularidad = Titularidad.builder()
                .asociado(asociado)
                .inmueble(inmueble)
                .fechaInicio(fechaInicio != null ? fechaInicio : LocalDate.now())
                .motivo(motivo)
                .observaciones(observaciones)
                .estado("ACTIVO")
                .build();

        return titularidadRepository.save(titularidad);
    }

    @Override
    public Titularidad finalizarTitularidad(Long idTitularidad, LocalDate fechaFin, String motivo, String observaciones) {
        Titularidad titularidad = obtenerPorId(idTitularidad);
        if (!"ACTIVO".equalsIgnoreCase(titularidad.getEstado()))
            throw new BusinessException("La titularidad ya se encuentra finalizada.");

        LocalDate fin = fechaFin != null ? fechaFin : LocalDate.now();
        if (titularidad.getFechaInicio() != null && fin.isBefore(titularidad.getFechaInicio()))
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio.");

        titularidad.setFechaFin(fin);
        titularidad.setMotivo(motivo);
        titularidad.setObservaciones(observaciones);
        titularidad.setEstado("FINALIZADO");
        return titularidadRepository.save(titularidad);
    }

    @Override
    public Titularidad cambiarTitular(Long idInmueble, Long idNuevoAsociado, LocalDate fechaCambio, String motivo, String observaciones) {
        LocalDate cambio = fechaCambio != null ? fechaCambio : LocalDate.now();

        titularidadRepository.findFirstByInmueble_IdInmuebleAndEstadoIgnoreCaseOrderByFechaInicioDesc(idInmueble, "ACTIVO")
                .ifPresent(actual -> {
                    actual.setFechaFin(cambio);
                    actual.setEstado("FINALIZADO");
                    actual.setMotivo(motivo);
                    titularidadRepository.save(actual);
                });

        return registrar(idNuevoAsociado, idInmueble, cambio, motivo, observaciones);
    }
}
