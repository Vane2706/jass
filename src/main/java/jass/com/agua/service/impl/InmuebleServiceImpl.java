package jass.com.agua.service.impl;

import jass.com.agua.entity.Inmueble;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.InmuebleRepository;
import jass.com.agua.service.InmuebleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InmuebleServiceImpl implements InmuebleService {

    private final InmuebleRepository inmuebleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Inmueble> listarTodos() {

        return inmuebleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inmueble> buscarDirecciones(
            String q,
            Pageable pageable
    ) {

        if (q == null || q.trim().length() < 2) {
            return List.of();
        }

        return inmuebleRepository.buscarDirecciones(
                q.trim(),
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inmueble> buscarActivosParaServicio(
            String texto,
            Pageable pageable
    ) {

        if (texto == null
                || texto.trim().length() < 2) {

            return List.of();
        }

        return inmuebleRepository
                .buscarActivosParaServicio(
                        texto.trim(),
                        pageable
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Inmueble obtenerPorId(Long id) {

        return inmuebleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inmueble no encontrado con id: " + id
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Inmueble obtenerPorCodigo(
            String codigoInmueble
    ) {

        return inmuebleRepository
                .findByCodigoInmueble(codigoInmueble)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inmueble no encontrado con código: "
                                        + codigoInmueble
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inmueble> buscar(
            String texto
    ) {

        if (texto == null || texto.isBlank()) {

            return inmuebleRepository.findAll();
        }

        return inmuebleRepository.buscar(
                texto.trim()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Inmueble> listarPaginado(
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
                        Sort.by(
                                Sort.Direction.DESC,
                                "idInmueble"
                        )
                );

        if (texto == null || texto.isBlank()) {

            return inmuebleRepository
                    .findAll(pageable);
        }

        return inmuebleRepository
                .buscarPaginado(
                        texto.trim(),
                        pageable
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inmueble> listarPorEstado(
            String estado
    ) {

        return inmuebleRepository
                .findByEstadoIgnoreCaseOrderByDireccionAsc(
                        estado
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inmueble> listarPorSector(
            String sector
    ) {

        return inmuebleRepository
                .findBySectorIgnoreCaseOrderByDireccionAsc(
                        sector
                );
    }

    @Override
    public Inmueble registrar(
            Inmueble inmueble
    ) {

        validar(inmueble);

        if (inmuebleRepository
                .existsByCodigoInmueble(
                        inmueble.getCodigoInmueble()
                )) {

            throw new BusinessException(
                    "Ya existe un inmueble con el código: "
                            + inmueble.getCodigoInmueble()
            );
        }

        inmueble.setIdInmueble(null);

        inmueble.setEstado(
                inmueble.getEstado() == null
                        || inmueble.getEstado().isBlank()
                        ? "ACTIVO"
                        : inmueble.getEstado()
                        .trim()
                        .toUpperCase()
        );

        return inmuebleRepository.save(inmueble);
    }

    @Override
    public Inmueble actualizar(
            Long id,
            Inmueble datos
    ) {

        Inmueble actual =
                obtenerPorId(id);

        validar(datos);

        inmuebleRepository
                .findByCodigoInmueble(
                        datos.getCodigoInmueble()
                )
                .filter(i ->
                        !i.getIdInmueble().equals(id)
                )
                .ifPresent(i -> {

                    throw new BusinessException(
                            "El código del inmueble ya está registrado."
                    );
                });

        actual.setCodigoInmueble(
                datos.getCodigoInmueble()
        );

        actual.setDireccion(
                datos.getDireccion()
        );

        actual.setReferencia(
                datos.getReferencia()
        );

        actual.setDistrito(
                datos.getDistrito()
        );

        actual.setSector(
                datos.getSector()
        );

        actual.setNumeroLote(
                datos.getNumeroLote()
        );

        actual.setManzana(
                datos.getManzana()
        );

        if (datos.getEstado() != null) {

            actual.setEstado(
                    datos.getEstado()
            );
        }

        return inmuebleRepository.save(actual);
    }

    @Override
    public Inmueble cambiarEstado(
            Long id,
            String estado
    ) {

        Inmueble inmueble =
                obtenerPorId(id);

        if (estado == null || estado.isBlank()) {

            throw new BusinessException(
                    "El estado es obligatorio."
            );
        }

        inmueble.setEstado(
                estado.trim().toUpperCase()
        );

        return inmuebleRepository.save(inmueble);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCodigo(
            String codigoInmueble
    ) {

        return inmuebleRepository
                .existsByCodigoInmueble(
                        codigoInmueble
                );
    }

    private void validar(
            Inmueble inmueble
    ) {

        if (inmueble == null) {

            throw new BusinessException(
                    "Los datos del inmueble son obligatorios."
            );
        }

        if (inmueble.getCodigoInmueble() == null
                || inmueble.getCodigoInmueble().isBlank()) {

            throw new BusinessException(
                    "El código del inmueble es obligatorio."
            );
        }

        if (inmueble.getDireccion() == null
                || inmueble.getDireccion().isBlank()) {

            throw new BusinessException(
                    "La dirección del inmueble es obligatoria."
            );
        }
    }
}