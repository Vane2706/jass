package jass.com.agua.service.impl;

import jass.com.agua.entity.Lectura;
import jass.com.agua.entity.Medidor;
import jass.com.agua.entity.Periodo;
import jass.com.agua.entity.Usuario;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.LecturaRepository;
import jass.com.agua.repository.MedidorRepository;
import jass.com.agua.repository.PeriodoRepository;
import jass.com.agua.repository.UsuarioRepository;
import jass.com.agua.service.LecturaService;
import jass.com.agua.service.PeriodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LecturaServiceImpl implements LecturaService {

    private final LecturaRepository lecturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MedidorRepository medidorRepository;
    private final PeriodoRepository periodoRepository;
    private final PeriodoService periodoService;

    @Override
    @Transactional(readOnly = true)
    public List<Lectura> listarTodas() { return lecturaRepository.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public Page<Lectura> listarPaginado(
            Long idPeriodo,
            Long idInmueble,
            int pagina,
            int tamanio
    ) {

        int paginaSegura =
                Math.max(pagina, 0);

        Pageable pageable =
                PageRequest.of(
                        paginaSegura,
                        tamanio,
                        Sort.by(
                                Sort.Order.desc("fechaLectura"),
                                Sort.Order.desc("idLectura")
                        )
                );

        return lecturaRepository.buscarPaginado(
                idPeriodo,
                idInmueble,
                pageable
        );
    }


    @Override
    @Transactional(readOnly = true)
    public Lectura obtenerPorId(Long id) {
        return lecturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lectura no encontrada con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lectura> listarPorMedidor(Long idMedidor) {
        return lecturaRepository.findByMedidor_IdMedidorOrderByFechaLecturaDesc(idMedidor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lectura> listarPorPeriodo(Long idPeriodo) {
        return lecturaRepository.findByPeriodo_IdPeriodoOrderByFechaLecturaAsc(idPeriodo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lectura> listarPorOperadorYPeriodo(Long idUsuario, Long idPeriodo) {
        return lecturaRepository.findByUsuario_IdUsuarioAndPeriodo_IdPeriodoOrderByFechaLecturaAsc(idUsuario, idPeriodo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lectura> listarPorEstadoSincronizacion(String estadoSincronizacion) {
        return lecturaRepository.findByEstadoSincronizacionIgnoreCaseOrderByFechaLecturaAsc(estadoSincronizacion);
    }

    @Override
    @Transactional(readOnly = true)
    public Lectura obtenerUltimaLecturaPorMedidor(Long idMedidor) {
        return lecturaRepository.findFirstByMedidor_IdMedidorOrderByFechaLecturaDesc(idMedidor)
                .orElseThrow(() -> new ResourceNotFoundException("No existen lecturas para el medidor: " + idMedidor));
    }

    @Override
    public Lectura registrarLectura(
            Long idUsuario,
            Long idMedidor,
            BigDecimal lecturaActual,
            String observaciones,
            String origen
    ) {

        if (lecturaActual == null ||
                lecturaActual.compareTo(BigDecimal.ZERO) < 0) {

            throw new BusinessException(
                    "La lectura actual debe ser mayor o igual a cero."
            );
        }

        Periodo periodo =
                periodoService.obtenerOCrearPeriodoActual();

        if ("CERRADO".equalsIgnoreCase(periodo.getEstado())) {
            throw new BusinessException(
                    "El periodo actual se encuentra cerrado."
            );
        }

        if (lecturaRepository
                .existsByMedidor_IdMedidorAndPeriodo_IdPeriodo(
                        idMedidor,
                        periodo.getIdPeriodo()
                )) {

            throw new BusinessException(
                    "Ya existe una lectura registrada para este medidor " +
                            "en el periodo " +
                            String.format(
                                    "%04d/%02d",
                                    periodo.getAnio(),
                                    periodo.getMes()
                            )
            );
        }

        Usuario usuario = usuarioRepository
                .findById(idUsuario)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado: " + idUsuario
                        )
                );

        Medidor medidor = medidorRepository
                .findById(idMedidor)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Medidor no encontrado: " + idMedidor
                        )
                );

        if (!"ACTIVO".equalsIgnoreCase(medidor.getEstado())) {
            throw new BusinessException(
                    "No se pueden registrar lecturas para un medidor inactivo."
            );
        }

        BigDecimal anterior = lecturaRepository
                .findFirstByMedidor_IdMedidorOrderByFechaLecturaDesc(
                        idMedidor
                )
                .map(Lectura::getLecturaActual)
                .orElse(
                        medidor.getLecturaInicial() != null
                                ? medidor.getLecturaInicial()
                                : BigDecimal.ZERO
                );

        if (lecturaActual.compareTo(anterior) < 0) {
            throw new BusinessException(
                    "La lectura actual no puede ser menor que la lectura anterior."
            );
        }

        BigDecimal consumo =
                lecturaActual.subtract(anterior);

        Lectura lectura = Lectura.builder()
                .usuario(usuario)
                .medidor(medidor)
                .periodo(periodo)
                .fechaLectura(LocalDateTime.now())
                .lecturaAnterior(anterior)
                .lecturaActual(lecturaActual)
                .consumo(consumo)
                .observaciones(observaciones)
                .origen(
                        origen == null || origen.isBlank()
                                ? "WEB"
                                : origen.trim().toUpperCase()
                )
                .estadoSincronizacion(
                        "WEB".equalsIgnoreCase(origen)
                                ? "SINCRONIZADO"
                                : "PENDIENTE"
                )
                .fechaSincronizacion(
                        "WEB".equalsIgnoreCase(origen)
                                ? LocalDateTime.now()
                                : null
                )
                .build();

        return lecturaRepository.save(lectura);
    }

    @Override
    public Lectura actualizarObservaciones(Long idLectura, String observaciones) {
        Lectura lectura = obtenerPorId(idLectura);
        lectura.setObservaciones(observaciones);
        return lecturaRepository.save(lectura);
    }

    @Override
    public Lectura marcarComoSincronizada(Long idLectura) {
        Lectura lectura = obtenerPorId(idLectura);
        lectura.setEstadoSincronizacion("SINCRONIZADO");
        lectura.setFechaSincronizacion(LocalDateTime.now());
        return lecturaRepository.save(lectura);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeLecturaParaPeriodo(Long idMedidor, Long idPeriodo) {
        return lecturaRepository.existsByMedidor_IdMedidorAndPeriodo_IdPeriodo(idMedidor, idPeriodo);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularConsumo(Long idMedidor, BigDecimal lecturaActual) {
        if (lecturaActual == null) throw new BusinessException("La lectura actual es obligatoria.");

        Medidor medidor = medidorRepository.findById(idMedidor)
                .orElseThrow(() -> new ResourceNotFoundException("Medidor no encontrado: " + idMedidor));

        BigDecimal anterior = lecturaRepository.findFirstByMedidor_IdMedidorOrderByFechaLecturaDesc(idMedidor)
                .map(Lectura::getLecturaActual)
                .orElse(medidor.getLecturaInicial() != null ? medidor.getLecturaInicial() : BigDecimal.ZERO);

        if (lecturaActual.compareTo(anterior) < 0)
            throw new BusinessException("La lectura actual no puede ser menor que la anterior.");

        return lecturaActual.subtract(anterior);
    }
}
