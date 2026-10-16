package jass.com.agua.service.impl;

import jass.com.agua.entity.Caja;
import jass.com.agua.entity.CierreCaja;
import jass.com.agua.entity.Usuario;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.CajaRepository;
import jass.com.agua.repository.CierreCajaRepository;
import jass.com.agua.repository.MovimientoCajaRepository;
import jass.com.agua.repository.UsuarioRepository;
import jass.com.agua.service.CajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CajaServiceImpl
        implements CajaService {

    private final CajaRepository cajaRepo;
    private final UsuarioRepository usuarioRepo;
    private final MovimientoCajaRepository movRepo;
    private final CierreCajaRepository cierreRepo;

    @Override
    @Transactional(readOnly = true)
    public List<Caja> listarTodos() {

        return cajaRepo.findAll(
                Sort.by(
                        Sort.Order.desc("fechaApertura"),
                        Sort.Order.desc("idCaja")
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerSaldoInicialSiguienteCaja() {

        return cajaRepo
                .findFirstByEstadoIgnoreCaseOrderByFechaAperturaDescIdCajaDesc(
                        "CERRADA"
                )
                .map(Caja::getSaldoFinal)
                .filter(Objects::nonNull)
                .orElse(BigDecimal.ZERO)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoActual(
            Long idCaja
    ) {

        Caja caja =
                obtenerPorId(
                        idCaja
                );


        BigDecimal inicial =
                caja.getSaldoInicial() != null
                        ? caja.getSaldoInicial()
                        : BigDecimal.ZERO;


        BigDecimal ingresos =
                movRepo.sumarPorCajaYTipo(
                        idCaja,
                        "INGRESO"
                );


        BigDecimal egresos =
                movRepo.sumarPorCajaYTipo(
                        idCaja,
                        "EGRESO"
                );


        if (ingresos == null) {
            ingresos = BigDecimal.ZERO;
        }


        if (egresos == null) {
            egresos = BigDecimal.ZERO;
        }


        return inicial
                .add(ingresos)
                .subtract(egresos)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Caja> listarPaginado(
            int pagina,
            int tamanio
    ) {

        int paginaSegura =
                Math.max(
                        pagina,
                        0
                );

        int tamanioSeguro =
                tamanio > 0
                        ? tamanio
                        : 5;


        Pageable pageable =
                PageRequest.of(
                        paginaSegura,
                        tamanioSeguro,

                        Sort.by(
                                Sort.Order.desc(
                                        "fechaApertura"
                                ),
                                Sort.Order.desc(
                                        "idCaja"
                                )
                        )
                );


        return cajaRepo.findAll(
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Caja obtenerPorId(
            Long id
    ) {

        return cajaRepo
                .findById(id)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Caja no encontrada: "
                                                + id
                                )
                );
    }

    @Override
    public Caja abrir(
            Long idUsuario,
            String nombre,
            BigDecimal saldo
    ) {

        Usuario usuario =
                usuarioRepo
                        .findById(idUsuario)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Usuario no encontrado: "
                                                        + idUsuario
                                        )
                        );


        if (
                cajaRepo
                        .findFirstByUsuario_IdUsuarioAndEstadoIgnoreCaseOrderByFechaAperturaDesc(
                                idUsuario,
                                "ABIERTA"
                        )
                        .isPresent()
        ) {

            throw new BusinessException(
                    "El usuario ya tiene una caja abierta."
            );
        }


        if (
                nombre == null
                        || nombre.isBlank()
        ) {

            throw new BusinessException(
                    "El nombre de la caja es obligatorio."
            );
        }


        BigDecimal saldoInicial =
                saldo == null
                        ? BigDecimal.ZERO
                        : saldo.setScale(
                        2,
                        RoundingMode.HALF_UP
                );


        if (
                saldoInicial.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {

            throw new BusinessException(
                    "El saldo inicial no puede ser negativo."
            );
        }


        Caja caja =
                Caja.builder()
                        .usuario(usuario)
                        .nombre(
                                nombre.trim()
                        )
                        .fechaApertura(
                                LocalDateTime.now()
                        )
                        .saldoInicial(
                                saldoInicial
                        )
                        .saldoFinal(null)
                        .estado("ABIERTA")
                        .build();


        return cajaRepo.save(
                caja
        );
    }

    @Override
    public Caja cerrar(
            Long idCaja,
            BigDecimal saldoReal
    ) {

        Caja caja =
                obtenerPorId(
                        idCaja
                );

        if (
                !"ABIERTA".equalsIgnoreCase(
                        caja.getEstado()
                )
        ) {

            throw new BusinessException(
                    "La caja no está abierta."
            );
        }

        if (saldoReal == null) {

            throw new BusinessException(
                    "Debe ingresar el monto contado en caja."
            );
        }


        BigDecimal real =
                saldoReal.setScale(
                        2,
                        RoundingMode.HALF_UP
                );


        if (
                real.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {

            throw new BusinessException(
                    "El monto ingresado no puede ser negativo."
            );
        }

        BigDecimal ingresos =
                movRepo.sumarPorCajaYTipo(
                        idCaja,
                        "INGRESO"
                );


        BigDecimal egresos =
                movRepo.sumarPorCajaYTipo(
                        idCaja,
                        "EGRESO"
                );


        if (ingresos == null) {
            ingresos = BigDecimal.ZERO;
        }


        if (egresos == null) {
            egresos = BigDecimal.ZERO;
        }


        BigDecimal inicial =
                caja.getSaldoInicial() != null
                        ? caja.getSaldoInicial()
                        : BigDecimal.ZERO;


        BigDecimal calculado =
                inicial
                        .add(ingresos)
                        .subtract(egresos)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        if (
                real.compareTo(
                        calculado
                ) != 0
        ) {

            throw new BusinessException(
                    "No se puede cerrar la caja. "
                            + "El monto ingresado debe coincidir "
                            + "exactamente con el saldo actual de S/ "
                            + calculado.toPlainString()
                            + "."
            );
        }

        BigDecimal diferencia =
                BigDecimal.ZERO
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        CierreCaja cierre =
                CierreCaja.builder()
                        .caja(caja)
                        .fechaCierre(
                                LocalDateTime.now()
                        )
                        .totalIngresos(
                                ingresos.setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        )
                        .totalEgresos(
                                egresos.setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        )
                        .saldoCalculado(
                                calculado
                        )
                        .saldoReal(
                                real
                        )
                        .diferencia(
                                diferencia
                        )
                        .build();


        cierreRepo.save(
                cierre
        );

        caja.setSaldoFinal(
                real
        );

        caja.setEstado(
                "CERRADA"
        );


        return cajaRepo.save(
                caja
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCajaAbierta() {

        return cajaRepo
                .existsByEstadoIgnoreCase(
                        "ABIERTA"
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Caja>
    obtenerCajaAbiertaUsuario(
            Long idUsuario
    ) {

        return cajaRepo
                .findFirstByUsuario_IdUsuarioAndEstadoIgnoreCaseOrderByFechaAperturaDesc(
                        idUsuario,
                        "ABIERTA"
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Caja> listarAbiertas() {

        return cajaRepo
                .findByEstadoIgnoreCaseOrderByFechaAperturaDesc(
                        "ABIERTA"
                );
    }
}