package jass.com.agua.service.impl;

import jass.com.agua.dto.MedidorLecturaDTO;
import jass.com.agua.entity.Inmueble;
import jass.com.agua.entity.Medidor;
import jass.com.agua.entity.Servicio;
import jass.com.agua.entity.Titularidad;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.MedidorRepository;
import jass.com.agua.repository.ServicioRepository;
import jass.com.agua.repository.TitularidadRepository;
import jass.com.agua.service.MedidorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MedidorServiceImpl implements MedidorService {

    private final MedidorRepository medidorRepository;
    private final ServicioRepository servicioRepository;
    private final TitularidadRepository titularidadRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Medidor> listarTodos() { return medidorRepository.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public Medidor obtenerPorId(Long id) {
        return medidorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medidor no encontrado con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Medidor obtenerPorNumero(String numeroMedidor) {
        return medidorRepository.findByNumeroMedidor(numeroMedidor)
                .orElseThrow(() -> new ResourceNotFoundException("Medidor no encontrado: " + numeroMedidor));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Medidor> listarPorServicio(Long idServicio) {
        return medidorRepository.findByServicio_IdServicioOrderByFechaInstalacionDesc(idServicio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Medidor> listarPorEstado(String estado) {
        return medidorRepository.findByEstadoIgnoreCaseOrderByNumeroMedidorAsc(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public Medidor obtenerMedidorActivoPorServicio(Long idServicio) {
        return medidorRepository.findFirstByServicio_IdServicioAndEstadoIgnoreCaseOrderByFechaInstalacionDesc(idServicio, "ACTIVO")
                .orElseThrow(() -> new ResourceNotFoundException("No existe medidor activo para el servicio: " + idServicio));
    }

    @Override
    public Medidor registrar(Medidor medidor) {

        if (medidor == null ||
                medidor.getNumeroMedidor() == null ||
                medidor.getNumeroMedidor().isBlank()) {

            throw new BusinessException(
                    "El número del medidor es obligatorio."
            );
        }

        if (medidor.getServicio() == null ||
                medidor.getServicio().getIdServicio() == null) {

            throw new BusinessException(
                    "Debe indicar el servicio."
            );
        }

        String numero = medidor.getNumeroMedidor().trim();

        if (medidorRepository.existsByNumeroMedidor(numero)) {
            throw new BusinessException(
                    "Ya existe un medidor con ese número."
            );
        }

        Servicio servicio = servicioRepository
                .findById(
                        medidor.getServicio().getIdServicio()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Servicio no encontrado."
                        )
                );

        String estado = medidor.getEstado();

        if (estado == null || estado.isBlank()) {
            estado = "ACTIVO";
        }

        estado = estado.trim().toUpperCase();

        if ("ACTIVO".equals(estado) &&
                medidorRepository
                        .existsByServicio_IdServicioAndEstadoIgnoreCase(
                                servicio.getIdServicio(),
                                "ACTIVO"
                        )) {

            throw new BusinessException(
                    "El servicio ya posee un medidor activo."
            );
        }

        medidor.setIdMedidor(null);
        medidor.setNumeroMedidor(numero);
        medidor.setServicio(servicio);
        medidor.setEstado(estado);

        if (medidor.getFechaInstalacion() == null) {
            medidor.setFechaInstalacion(LocalDate.now());
        }

        if (medidor.getLecturaInicial() == null) {
            medidor.setLecturaInicial(BigDecimal.ZERO);
        }

        if (medidor.getLecturaInicial()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new BusinessException(
                    "La lectura inicial no puede ser negativa."
            );
        }

        return medidorRepository.save(medidor);
    }

    @Override
    public Medidor actualizar(Long id, Medidor datos) {
        Medidor actual = obtenerPorId(id);

        medidorRepository.findByNumeroMedidor(datos.getNumeroMedidor())
                .filter(m -> !m.getIdMedidor().equals(id))
                .ifPresent(m -> { throw new BusinessException("El número del medidor ya está registrado."); });

        if (datos.getServicio() != null && datos.getServicio().getIdServicio() != null) {
            actual.setServicio(servicioRepository.findById(datos.getServicio().getIdServicio())
                    .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado.")));
        }

        actual.setNumeroMedidor(datos.getNumeroMedidor());
        actual.setMarca(datos.getMarca());
        actual.setModelo(datos.getModelo());
        actual.setFechaInstalacion(datos.getFechaInstalacion());
        actual.setLecturaInicial(datos.getLecturaInicial());
        if (datos.getEstado() != null) actual.setEstado(datos.getEstado());
        return medidorRepository.save(actual);
    }

    @Override
    public Medidor cambiarEstado(Long id, String estado) {
        Medidor medidor = obtenerPorId(id);
        if (estado == null || estado.isBlank()) throw new BusinessException("El estado es obligatorio.");
        medidor.setEstado(estado.trim().toUpperCase());
        return medidorRepository.save(medidor);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeNumero(String numeroMedidor) {
        return medidorRepository.existsByNumeroMedidor(numeroMedidor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedidorLecturaDTO> buscarParaLectura(String q) {

        if (q == null || q.isBlank()) {
            return List.of();
        }

        String busqueda = q.trim();

        return medidorRepository.buscarParaLectura(busqueda)
                .stream()
                .limit(10)
                .map(m -> {

                    Servicio servicio = m.getServicio();
                    Inmueble inmueble = servicio.getInmueble();

                    String nombreAsociado =
                            titularidadRepository
                                    .findFirstByInmueble_IdInmuebleAndEstadoIgnoreCaseOrderByFechaInicioDesc(
                                            inmueble.getIdInmueble(),
                                            "ACTIVO"
                                    )
                                    .map(Titularidad::getAsociado)
                                    .map(a -> {

                                        StringBuilder nombre = new StringBuilder();

                                        if (a.getNombres() != null) {
                                            nombre.append(a.getNombres());
                                        }

                                        if (a.getApellidoPaterno() != null) {
                                            nombre.append(" ")
                                                    .append(a.getApellidoPaterno());
                                        }

                                        if (a.getApellidoMaterno() != null) {
                                            nombre.append(" ")
                                                    .append(a.getApellidoMaterno());
                                        }

                                        return nombre.toString().trim();
                                    })
                                    .orElse("Sin titular activo");

                    return new MedidorLecturaDTO(
                            m.getIdMedidor(),
                            m.getNumeroMedidor(),
                            servicio.getCodigoServicio(),
                            inmueble.getCodigoInmueble(),
                            inmueble.getDireccion(),
                            inmueble.getReferencia(),
                            inmueble.getSector(),
                            nombreAsociado
                    );
                })
                .toList();
    }
}
