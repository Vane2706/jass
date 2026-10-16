package jass.com.agua.service.impl;

import jass.com.agua.entity.AsignacionOperador;
import jass.com.agua.entity.Servicio;
import jass.com.agua.entity.Usuario;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.AsignacionOperadorRepository;
import jass.com.agua.repository.ServicioRepository;
import jass.com.agua.repository.UsuarioRepository;
import jass.com.agua.repository.UsuarioRolRepository;
import jass.com.agua.service.AsignacionOperadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AsignacionOperadorServiceImpl implements AsignacionOperadorService {

    private final AsignacionOperadorRepository asignacionOperadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionOperador> listarTodas() { return asignacionOperadorRepository.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public AsignacionOperador obtenerPorId(Long id) {
        return asignacionOperadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionOperador> listarPorOperador(Long idUsuario) {
        return asignacionOperadorRepository.findByUsuario_IdUsuarioAndEstadoIgnoreCaseOrderByFechaAsignacionDesc(idUsuario, "ACTIVO");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionOperador> listarPorServicio(Long idServicio) {
        return asignacionOperadorRepository.findByServicio_IdServicioOrderByFechaAsignacionDesc(idServicio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionOperador> listarAsignacionesVigentes(Long idUsuario, LocalDate fecha) {
        return asignacionOperadorRepository.buscarAsignacionesVigentes(
                idUsuario,
                fecha != null ? fecha : LocalDate.now(),
                "ACTIVO"
        );
    }

    @Override
    public AsignacionOperador asignar(Long idUsuario, Long idServicio, LocalDate fechaAsignacion, LocalDate fechaFin) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + idUsuario));
        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + idServicio));

        if (asignacionOperadorRepository.existsByUsuario_IdUsuarioAndServicio_IdServicioAndEstadoIgnoreCase(idUsuario, idServicio, "ACTIVO"))
            throw new BusinessException("El operador ya tiene este servicio asignado.");

        LocalDate inicio = fechaAsignacion != null ? fechaAsignacion : LocalDate.now();
        if (fechaFin != null && fechaFin.isBefore(inicio))
            throw new BusinessException("La fecha fin no puede ser anterior a la fecha de asignación.");

        AsignacionOperador asignacion = AsignacionOperador.builder()
                .usuario(usuario)
                .servicio(servicio)
                .fechaAsignacion(inicio)
                .fechaFin(fechaFin)
                .estado("ACTIVO")
                .build();

        return asignacionOperadorRepository.save(asignacion);
    }

    @Override
    public AsignacionOperador finalizarAsignacion(Long idAsignacion, LocalDate fechaFin) {
        AsignacionOperador asignacion = obtenerPorId(idAsignacion);
        LocalDate fin = fechaFin != null ? fechaFin : LocalDate.now();

        if (asignacion.getFechaAsignacion() != null && fin.isBefore(asignacion.getFechaAsignacion()))
            throw new BusinessException("La fecha fin no puede ser anterior a la fecha de asignación.");

        asignacion.setFechaFin(fin);
        asignacion.setEstado("FINALIZADO");
        return asignacionOperadorRepository.save(asignacion);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeAsignacionActiva(Long idUsuario, Long idServicio) {
        return asignacionOperadorRepository.existsByUsuario_IdUsuarioAndServicio_IdServicioAndEstadoIgnoreCase(
                idUsuario, idServicio, "ACTIVO"
        );
    }


    @Override
    public void sincronizarAsignacionesAutomaticas() {

        List<Usuario> operadores =
                usuarioRolRepository.buscarUsuariosOperadoresActivos();

        List<Servicio> servicios =
                servicioRepository.findByEstadoIgnoreCaseOrderByCodigoServicioAsc(
                        "ACTIVO"
                );

        LocalDate hoy = LocalDate.now();

        for (Usuario operador : operadores) {
            for (Servicio servicio : servicios) {

                boolean yaAsignado =
                        asignacionOperadorRepository
                                .existeAsignacionVigente(
                                        operador.getIdUsuario(),
                                        servicio.getIdServicio(),
                                        hoy
                                );

                if (!yaAsignado) {
                    AsignacionOperador nueva =
                            AsignacionOperador.builder()
                                    .usuario(operador)
                                    .servicio(servicio)
                                    .fechaAsignacion(hoy)
                                    .fechaFin(null)
                                    .estado("ACTIVO")
                                    .build();

                    asignacionOperadorRepository.save(nueva);
                }
            }
        }
    }

}
