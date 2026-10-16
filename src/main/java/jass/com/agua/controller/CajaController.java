package jass.com.agua.controller;

import jass.com.agua.entity.Caja;
import jass.com.agua.entity.MovimientoCaja;
import jass.com.agua.entity.Usuario;
import jass.com.agua.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService caja;
    private final MovimientoCajaService movimiento;
    private final CierreCajaService cierre;
    private final UsuarioService usuario;
    private final AuditoriaService auditoria;


    private static final int TAMANIO_PAGINA = 5;

    private static final int TAMANIO_MOVIMIENTOS = 5;

    private static final int MAX_NUMEROS_PAGINACION = 10;

    @GetMapping
    public String listar(

            @RequestParam(
                    defaultValue = "0"
            )
            int pagina,

            Authentication authentication,

            Model m
    ) {

        if (pagina < 0) {
            pagina = 0;
        }


        Usuario usuarioSesion =
                usuario.obtenerPorUsername(
                        authentication.getName()
                );


        Page<Caja> resultado =
                caja.listarPaginado(
                        pagina,
                        TAMANIO_PAGINA
                );


        if (
                resultado.getTotalPages() > 0
                        &&
                        pagina >= resultado.getTotalPages()
        ) {

            pagina =
                    resultado.getTotalPages() - 1;


            resultado =
                    caja.listarPaginado(
                            pagina,
                            TAMANIO_PAGINA
                    );
        }


        PaginacionVista paginacion =
                calcularPaginacion(
                        resultado.getNumber(),
                        resultado.getTotalPages()
                );


        m.addAttribute(
                "cajas",
                resultado.getContent()
        );

        m.addAttribute(
                "paginaActual",
                resultado.getNumber()
        );

        m.addAttribute(
                "totalPaginas",
                resultado.getTotalPages()
        );

        m.addAttribute(
                "totalRegistros",
                resultado.getTotalElements()
        );

        m.addAttribute(
                "esPrimera",
                resultado.isFirst()
        );

        m.addAttribute(
                "esUltima",
                resultado.isLast()
        );

        m.addAttribute(
                "inicioPagina",
                paginacion.inicio()
        );

        m.addAttribute(
                "finPagina",
                paginacion.fin()
        );

        m.addAttribute(
                "puntosIzquierda",
                paginacion.puntosIzquierda()
        );

        m.addAttribute(
                "puntosDerecha",
                paginacion.puntosDerecha()
        );

        m.addAttribute(
                "usuarioSesion",
                usuarioSesion
        );

        m.addAttribute(
                "saldoInicialSugerido",
                caja.obtenerSaldoInicialSiguienteCaja()
        );

        return "caja/lista";
    }

    @PostMapping("/abrir")
    public String abrir(

            Authentication authentication,

            @RequestParam
            String nombre,

            @RequestParam(
                    required = false
            )
            BigDecimal saldoInicial
    ) {

        Usuario usuarioSesion =
                usuario.obtenerPorUsername(
                        authentication.getName()
                );


        Caja c =
                caja.abrir(
                        usuarioSesion.getIdUsuario(),
                        nombre,
                        saldoInicial
                );

        auditoria.registrarEvento(
                "ABRIR",
                "CAJA",
                c.getIdCaja(),
                "Caja abierta por el usuario: "
                        + usuarioSesion.getUsername()
        );


        return "redirect:/caja/"
                + c.getIdCaja();
    }

    @GetMapping("/{id:\\d+}")
    public String detalle(

            @PathVariable
            Long id,

            @RequestParam(
                    defaultValue = "0"
            )
            int paginaMovimiento,

            Model m
    ) {

        if (paginaMovimiento < 0) {
            paginaMovimiento = 0;
        }


        Caja cajaActual =
                caja.obtenerPorId(
                        id
                );


        Page<MovimientoCaja> resultado =
                movimiento.listarPorCajaPaginado(
                        id,
                        paginaMovimiento,
                        TAMANIO_MOVIMIENTOS
                );

        if (
                resultado.getTotalPages() > 0
                        &&
                        paginaMovimiento
                                >= resultado.getTotalPages()
        ) {

            paginaMovimiento =
                    resultado.getTotalPages() - 1;


            resultado =
                    movimiento.listarPorCajaPaginado(
                            id,
                            paginaMovimiento,
                            TAMANIO_MOVIMIENTOS
                    );
        }


        PaginacionVista paginacionMovimiento =
                calcularPaginacion(
                        resultado.getNumber(),
                        resultado.getTotalPages()
                );

        m.addAttribute(
                "caja",
                cajaActual
        );


        m.addAttribute(
                "saldoActual",
                caja.calcularSaldoActual(id)
        );

        m.addAttribute(
                "movimientos",
                resultado.getContent()
        );

        m.addAttribute(
                "paginaMovimientoActual",
                resultado.getNumber()
        );

        m.addAttribute(
                "totalPaginasMovimientos",
                resultado.getTotalPages()
        );

        m.addAttribute(
                "totalMovimientos",
                resultado.getTotalElements()
        );

        m.addAttribute(
                "movimientosPrimeraPagina",
                resultado.isFirst()
        );

        m.addAttribute(
                "movimientosUltimaPagina",
                resultado.isLast()
        );

        m.addAttribute(
                "inicioPaginaMovimiento",
                paginacionMovimiento.inicio()
        );

        m.addAttribute(
                "finPaginaMovimiento",
                paginacionMovimiento.fin()
        );

        m.addAttribute(
                "puntosIzquierdaMovimiento",
                paginacionMovimiento.puntosIzquierda()
        );

        m.addAttribute(
                "puntosDerechaMovimiento",
                paginacionMovimiento.puntosDerecha()
        );

        m.addAttribute(
                "cierres",
                cierre.listarPorCaja(id)
        );


        return "caja/detalle";
    }

    @PostMapping("/{id:\\d+}/movimiento")
    public String movimiento(

            @PathVariable
            Long id,

            @RequestParam
            String tipo,

            @RequestParam
            String concepto,

            @RequestParam
            BigDecimal monto,

            @RequestParam(
                    required = false
            )
            String observaciones
    ) {

        Caja cajaActual =
                caja.obtenerPorId(
                        id
                );

        movimiento.registrar(
                id,
                cajaActual
                        .getUsuario()
                        .getIdUsuario(),
                null,
                tipo,
                concepto,
                monto,
                observaciones
        );

        return "redirect:/caja/"
                + id
                + "?paginaMovimiento=0";
    }

    @PostMapping("/{id:\\d+}/cerrar")
    public String cerrar(

            @PathVariable
            Long id,

            @RequestParam
            BigDecimal saldoReal
    ) {

        caja.cerrar(
                id,
                saldoReal
        );

        auditoria.registrarEvento(
                "CERRAR",
                "CAJA",
                id,
                "Caja cerrada. Saldo contado: " + saldoReal
        );

        return "redirect:/caja/" + id;
    }

    private PaginacionVista calcularPaginacion(
            int paginaActual,
            int totalPaginas
    ) {

        if (totalPaginas <= 0) {

            return new PaginacionVista(
                    0,
                    -1,
                    false,
                    false
            );
        }


        if (
                totalPaginas
                        <= MAX_NUMEROS_PAGINACION
        ) {

            return new PaginacionVista(
                    0,
                    totalPaginas - 1,
                    false,
                    false
            );
        }


        int inicio =
                Math.max(
                        1,
                        paginaActual - 3
                );


        int fin =
                Math.min(
                        totalPaginas - 2,
                        inicio + 7
                );


        inicio =
                Math.max(
                        1,
                        fin - 7
                );


        boolean puntosIzquierda =
                inicio > 1;


        boolean puntosDerecha =
                fin < totalPaginas - 2;


        return new PaginacionVista(
                inicio,
                fin,
                puntosIzquierda,
                puntosDerecha
        );
    }


    private record PaginacionVista(
            int inicio,
            int fin,
            boolean puntosIzquierda,
            boolean puntosDerecha
    ) {
    }
}
