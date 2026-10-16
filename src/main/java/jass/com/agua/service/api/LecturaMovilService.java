package jass.com.agua.service.api;

import jass.com.agua.dto.api.LecturaMovilRequest;
import jass.com.agua.dto.api.MedidorMovilDTO;
import jass.com.agua.entity.*;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.repository.*;
import jass.com.agua.service.FacturaService;
import jass.com.agua.service.TitularidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LecturaMovilService {
    private static final ZoneId ZONA = ZoneId.of("America/Lima");
    private final MedidorRepository medidorRepository;
    private final LecturaRepository lecturaRepository;
    private final PeriodoRepository periodoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignacionOperadorRepository asignacionRepository;
    private final TitularidadService titularidadService;
    private final FacturaService facturaService;

    @Transactional(readOnly = true)
    public List<MedidorMovilDTO> listarMedidores(Long idUsuario, String busqueda) {
        LocalDate hoy = LocalDate.now(ZONA);
        String q = busqueda == null ? "" : busqueda.trim();
        List<Medidor> medidores = medidorRepository.buscarMedidoresAsignados(idUsuario, hoy, q);
        if (medidores.isEmpty()) return List.of();
        List<Long> idsInmuebles = medidores.stream().map(Medidor::getServicio).filter(Objects::nonNull)
                .map(Servicio::getInmueble).filter(Objects::nonNull).map(Inmueble::getIdInmueble).distinct().toList();
        Map<Long, Asociado> titulares = titularidadService.obtenerTitularesActivosPorInmuebles(idsInmuebles);
        YearMonth ym = YearMonth.now(ZONA);
        Periodo periodo = periodoRepository.findByAnioAndMes(ym.getYear(), ym.getMonthValue()).orElse(null);
        Map<Long, Lectura> registradas = new HashMap<>();
        Map<Long, Lectura> anteriores = new HashMap<>();
        List<Long> idsMedidores = medidores.stream().map(Medidor::getIdMedidor).toList();
        if (periodo != null) {
            lecturaRepository.findByMedidor_IdMedidorInAndPeriodo_IdPeriodo(idsMedidores, periodo.getIdPeriodo())
                    .forEach(l -> registradas.put(l.getMedidor().getIdMedidor(), l));
            lecturaRepository.buscarUltimasLecturasPrevias(idsMedidores, periodo.getIdPeriodo())
                    .forEach(l -> anteriores.putIfAbsent(l.getMedidor().getIdMedidor(), l));
        } else {
            lecturaRepository.buscarUltimasLecturasPorMedidores(idsMedidores)
                    .forEach(l -> anteriores.putIfAbsent(l.getMedidor().getIdMedidor(), l));
        }
        String periodoTexto = String.format(Locale.ROOT, "%04d-%02d", ym.getYear(), ym.getMonthValue());
        return medidores.stream().map(m -> {
            Servicio s = m.getServicio(); Inmueble i = s.getInmueble();
            Asociado a = titulares.get(i.getIdInmueble());
            Lectura previa = anteriores.get(m.getIdMedidor());
            BigDecimal lecturaAnterior = previa != null ? previa.getLecturaActual()
                    : (m.getLecturaInicial() == null ? BigDecimal.ZERO : m.getLecturaInicial());
            return new MedidorMovilDTO(m.getIdMedidor(), m.getNumeroMedidor(), s.getCodigoServicio(),
                    i.getCodigoInmueble(), i.getDireccion(), i.getSector(), nombre(a),
                    a == null ? null : a.getCodigoAsociado(), lecturaAnterior, periodoTexto,
                    registradas.containsKey(m.getIdMedidor()));
        }).toList();
    }

    @Transactional
    public Lectura registrar(Long idUsuario, LecturaMovilRequest req) {
        if (req == null || req.idMedidor() == null) throw new BusinessException("Selecciona un medidor válido.");
        BigDecimal actual = req.lecturaActual();
        if (actual == null || actual.compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessException("La lectura actual debe ser un número mayor o igual a cero.");
        LocalDateTime fecha = req.fechaCaptura() == null ? LocalDateTime.now(ZONA) : req.fechaCaptura();
        if (fecha.isAfter(LocalDateTime.now(ZONA).plusMinutes(10)))
            throw new BusinessException("La fecha de captura no puede estar en el futuro.");
        Medidor medidor = medidorRepository.findById(req.idMedidor())
                .orElseThrow(() -> new BusinessException("El medidor seleccionado no existe."));
        if (!"ACTIVO".equalsIgnoreCase(medidor.getEstado()) || medidor.getServicio() == null
                || !"ACTIVO".equalsIgnoreCase(medidor.getServicio().getEstado()))
            throw new BusinessException("El medidor o su servicio no está activo.");
        boolean asignado = asignacionRepository.existeAsignacionVigente(
                idUsuario, medidor.getServicio().getIdServicio(), fecha.toLocalDate());
        if (!asignado) throw new BusinessException("Este medidor no está asignado a tu usuario para la fecha de captura.");

        YearMonth ym = YearMonth.from(fecha);
        Periodo periodo = periodoRepository.findByAnioAndMes(ym.getYear(), ym.getMonthValue()).orElseGet(() ->
                periodoRepository.save(Periodo.builder().anio(ym.getYear()).mes(ym.getMonthValue())
                        .fechaInicio(ym.atDay(1)).fechaFin(ym.atEndOfMonth()).estado("ABIERTO").build()));
        if (req.clientId() != null && !req.clientId().isBlank()) {
            var existentePorCaptura = lecturaRepository.findByIdCapturaMovil(req.clientId().trim());
            if (existentePorCaptura.isPresent()) {
                if (existentePorCaptura.get().getMedidor().getIdMedidor().equals(medidor.getIdMedidor()))
                    return existentePorCaptura.get();
                throw new BusinessException("El identificador local de captura ya fue utilizado para otro medidor.");
            }
        }
        if ("CERRADO".equalsIgnoreCase(periodo.getEstado()))
            throw new BusinessException("El periodo " + ym + " está cerrado; no se puede sincronizar esta lectura.");
        if (lecturaRepository.existsByMedidor_IdMedidorAndPeriodo_IdPeriodo(medidor.getIdMedidor(), periodo.getIdPeriodo()))
            throw new BusinessException("DUPLICADA: ya existe una lectura para este medidor en el periodo " + ym + ".");

        BigDecimal anterior = lecturaRepository
                .findFirstByMedidor_IdMedidorAndFechaLecturaBeforeOrderByFechaLecturaDescIdLecturaDesc(medidor.getIdMedidor(), fecha)
                .map(Lectura::getLecturaActual)
                .orElse(medidor.getLecturaInicial() == null ? BigDecimal.ZERO : medidor.getLecturaInicial());
        if (actual.compareTo(anterior) < 0)
            throw new BusinessException("La lectura actual (" + actual + ") no puede ser menor que la lectura anterior (" + anterior + ").");
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new BusinessException("El usuario operador ya no existe."));

        Lectura guardada = lecturaRepository.save(
                Lectura.builder()
                        .usuario(usuario)
                        .medidor(medidor)
                        .periodo(periodo)
                        .fechaLectura(fecha)
                        .lecturaAnterior(anterior)
                        .lecturaActual(actual)
                        .consumo(actual.subtract(anterior))
                        .observaciones(limpiar(req.observaciones()))
                        .origen("MOVIL")
                        .estadoSincronizacion("SINCRONIZADO")
                        .fechaSincronizacion(LocalDateTime.now(ZONA))
                        .idCapturaMovil(
                                req.clientId() == null || req.clientId().isBlank()
                                        ? null
                                        : req.clientId().trim()
                        )
                        .build()
        );

// Generar factura pendiente con la lógica de negocio existente.
        Long idServicio = medidor.getServicio().getIdServicio();

        if (!facturaService.existeFacturaParaPeriodo(
                idServicio,
                periodo.getIdPeriodo())) {

            facturaService.generarFactura(
                    idServicio,
                    periodo.getIdPeriodo()
            );
        }

        return guardada;

    }

    private String nombre(Asociado a) {
        if (a == null) return "Sin titular activo";
        return String.join(" ", Arrays.asList(a.getNombres(), a.getApellidoPaterno(), a.getApellidoMaterno())
                .stream().filter(Objects::nonNull).map(String::trim).filter(x -> !x.isBlank()).toList());
    }
    private String limpiar(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
