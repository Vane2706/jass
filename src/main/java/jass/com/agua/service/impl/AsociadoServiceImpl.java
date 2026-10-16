package jass.com.agua.service.impl;

import jass.com.agua.entity.Asociado;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.AsociadoRepository;
import jass.com.agua.service.AsociadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AsociadoServiceImpl implements AsociadoService {

    private final AsociadoRepository asociadoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Asociado> listarTodos() {
        return asociadoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asociado> buscarActivosParaTitular(
            String texto,
            Pageable pageable
    ) {

        if (texto == null
                || texto.trim().length() < 2) {

            return List.of();
        }

        return asociadoRepository.buscarActivosParaTitular(
                texto.trim(),
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Asociado obtenerPorId(Long id) {
        return asociadoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Asociado no encontrado con id: " + id
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Asociado obtenerPorCodigo(String codigoAsociado) {
        return asociadoRepository
                .findByCodigoAsociado(codigoAsociado)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Asociado no encontrado con código: "
                                        + codigoAsociado
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Asociado obtenerPorDocumento(String documento) {
        return asociadoRepository
                .findByDocumento(documento)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Asociado no encontrado con documento: "
                                        + documento
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asociado> buscar(String texto) {

        if (texto == null || texto.isBlank()) {
            return asociadoRepository.findAll();
        }

        return asociadoRepository.buscar(texto.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Asociado> listarPaginado(
            String texto,
            int pagina,
            int tamanio
    ) {

        int paginaSegura = Math.max(pagina, 0);

        Pageable pageable = PageRequest.of(
                paginaSegura,
                tamanio,
                Sort.by(
                        Sort.Direction.DESC,
                        "idAsociado"
                )
        );

        if (texto == null || texto.isBlank()) {

            return asociadoRepository.findAll(pageable);
        }

        return asociadoRepository.buscarPaginado(
                texto.trim(),
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asociado> listarPorEstado(String estado) {

        return asociadoRepository
                .findByEstadoIgnoreCaseOrderByApellidoPaternoAscNombresAsc(
                        estado
                );
    }

    @Override
    public Asociado registrar(Asociado asociado) {

        validarObligatorios(asociado);

        if (asociadoRepository
                .existsByCodigoAsociado(
                        asociado.getCodigoAsociado()
                )) {

            throw new BusinessException(
                    "Ya existe un asociado con el código: "
                            + asociado.getCodigoAsociado()
            );
        }

        if (asociado.getDocumento() != null
                && !asociado.getDocumento().isBlank()
                && asociadoRepository
                .existsByDocumento(asociado.getDocumento())) {

            throw new BusinessException(
                    "Ya existe un asociado con el documento: "
                            + asociado.getDocumento()
            );
        }

        asociado.setIdAsociado(null);

        asociado.setFechaRegistro(
                asociado.getFechaRegistro() != null
                        ? asociado.getFechaRegistro()
                        : LocalDateTime.now()
        );

        asociado.setEstado(
                normalizarEstado(
                        asociado.getEstado(),
                        "ACTIVO"
                )
        );

        return asociadoRepository.save(asociado);
    }

    @Override
    public Asociado actualizar(
            Long id,
            Asociado datos
    ) {

        Asociado actual = obtenerPorId(id);

        validarObligatorios(datos);

        asociadoRepository
                .findByCodigoAsociado(
                        datos.getCodigoAsociado()
                )
                .filter(a ->
                        !a.getIdAsociado().equals(id)
                )
                .ifPresent(a -> {
                    throw new BusinessException(
                            "El código de asociado ya está registrado."
                    );
                });

        if (datos.getDocumento() != null
                && !datos.getDocumento().isBlank()) {

            asociadoRepository
                    .findByDocumento(
                            datos.getDocumento()
                    )
                    .filter(a ->
                            !a.getIdAsociado().equals(id)
                    )
                    .ifPresent(a -> {
                        throw new BusinessException(
                                "El documento ya está registrado."
                        );
                    });
        }

        actual.setCodigoAsociado(
                datos.getCodigoAsociado()
        );

        actual.setTipoPersona(
                datos.getTipoPersona()
        );

        actual.setNombres(
                datos.getNombres()
        );

        actual.setApellidoPaterno(
                datos.getApellidoPaterno()
        );

        actual.setApellidoMaterno(
                datos.getApellidoMaterno()
        );

        actual.setDocumento(
                datos.getDocumento()
        );

        actual.setTelefono(
                datos.getTelefono()
        );

        actual.setCorreo(
                datos.getCorreo()
        );

        actual.setDireccionReferencia(
                datos.getDireccionReferencia()
        );

        if (datos.getEstado() != null) {
            actual.setEstado(
                    datos.getEstado()
            );
        }

        return asociadoRepository.save(actual);
    }

    @Override
    public Asociado cambiarEstado(
            Long id,
            String estado
    ) {

        Asociado asociado =
                obtenerPorId(id);

        asociado.setEstado(
                normalizarEstado(
                        estado,
                        asociado.getEstado()
                )
        );

        return asociadoRepository.save(asociado);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCodigo(
            String codigoAsociado
    ) {

        return asociadoRepository
                .existsByCodigoAsociado(
                        codigoAsociado
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeDocumento(
            String documento
    ) {

        return asociadoRepository
                .existsByDocumento(
                        documento
                );
    }

    private void validarObligatorios(
            Asociado asociado
    ) {

        if (asociado == null) {
            throw new BusinessException(
                    "Los datos del asociado son obligatorios."
            );
        }

        if (asociado.getCodigoAsociado() == null
                || asociado.getCodigoAsociado().isBlank()) {

            throw new BusinessException(
                    "El código del asociado es obligatorio."
            );
        }

        if (asociado.getNombres() == null
                || asociado.getNombres().isBlank()) {

            throw new BusinessException(
                    "El nombre del asociado es obligatorio."
            );
        }
    }

    private String normalizarEstado(
            String estado,
            String valorPorDefecto
    ) {

        return (estado == null || estado.isBlank())
                ? valorPorDefecto
                : estado.trim().toUpperCase();
    }
}