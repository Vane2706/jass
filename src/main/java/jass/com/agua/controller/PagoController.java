package jass.com.agua.controller;

import jass.com.agua.entity.Asociado;
import jass.com.agua.entity.Factura;
import jass.com.agua.entity.Pago;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pago;
    private final DetallePagoService detalle;
    private final FacturaService factura;
    private final UsuarioService usuario;
    private final CajaService caja;
    private final AuditoriaService auditoria;
    private static final int TAMANIO_PAGINA = 5;
    private static final int MAX_NUMEROS_PAGINACION = 10;


    @GetMapping
    public String listar(

            @RequestParam(
                    defaultValue = "0"
            ) int paginaPendientes,

            @RequestParam(
                    defaultValue = "0"
            ) int paginaPagos,

            Model m
    ) {
        paginaPendientes =
                Math.max(
                        paginaPendientes,
                        0
                );

        paginaPagos =
                Math.max(
                        paginaPagos,
                        0
                );
        Page<Factura> resultadoPendientes =
                factura.listarFacturasPorCobrarPaginado(
                        paginaPendientes,
                        TAMANIO_PAGINA
                );


        // Si solicitan una página que ya no existe
        if (resultadoPendientes.getTotalPages() > 0
                && paginaPendientes
                >= resultadoPendientes.getTotalPages()) {

            paginaPendientes =
                    resultadoPendientes
                            .getTotalPages() - 1;

            resultadoPendientes =
                    factura.listarFacturasPorCobrarPaginado(
                            paginaPendientes,
                            TAMANIO_PAGINA
                    );
        }

        Map<Long, Asociado> asociadosPorFactura =
                new LinkedHashMap<>();


        for (Factura f : resultadoPendientes.getContent()) {

            Asociado asociado =
                    factura.obtenerAsociadoFactura(
                            f.getIdFactura()
                    );

            asociadosPorFactura.put(
                    f.getIdFactura(),
                    asociado
            );
        }

        Page<Pago> resultadoPagos =
                pago.listarPaginado(
                        paginaPagos,
                        TAMANIO_PAGINA
                );


        if (resultadoPagos.getTotalPages() > 0
                && paginaPagos
                >= resultadoPagos.getTotalPages()) {

            paginaPagos =
                    resultadoPagos
                            .getTotalPages() - 1;

            resultadoPagos =
                    pago.listarPaginado(
                            paginaPagos,
                            TAMANIO_PAGINA
                    );
        }

        PaginacionVista pagPendientes =
                calcularPaginacion(
                        resultadoPendientes.getNumber(),
                        resultadoPendientes.getTotalPages()
                );
        PaginacionVista pagPagos =
                calcularPaginacion(
                        resultadoPagos.getNumber(),
                        resultadoPagos.getTotalPages()
                );
        m.addAttribute(
                "facturas",
                resultadoPendientes.getContent()
        );

        m.addAttribute(
                "asociadosPorFactura",
                asociadosPorFactura
        );

        m.addAttribute(
                "paginaPendientes",
                resultadoPendientes.getNumber()
        );

        m.addAttribute(
                "totalPaginasPendientes",
                resultadoPendientes.getTotalPages()
        );

        m.addAttribute(
                "totalPendientes",
                resultadoPendientes.getTotalElements()
        );

        m.addAttribute(
                "primeraPendientes",
                resultadoPendientes.isFirst()
        );

        m.addAttribute(
                "ultimaPendientes",
                resultadoPendientes.isLast()
        );

        m.addAttribute(
                "inicioPendientes",
                pagPendientes.inicio()
        );

        m.addAttribute(
                "finPendientes",
                pagPendientes.fin()
        );

        m.addAttribute(
                "puntosIzquierdaPendientes",
                pagPendientes.puntosIzquierda()
        );

        m.addAttribute(
                "puntosDerechaPendientes",
                pagPendientes.puntosDerecha()
        );
        m.addAttribute(
                "pagos",
                resultadoPagos.getContent()
        );

        m.addAttribute(
                "paginaPagos",
                resultadoPagos.getNumber()
        );

        m.addAttribute(
                "totalPaginasPagos",
                resultadoPagos.getTotalPages()
        );

        m.addAttribute(
                "totalPagos",
                resultadoPagos.getTotalElements()
        );

        m.addAttribute(
                "primeraPagos",
                resultadoPagos.isFirst()
        );

        m.addAttribute(
                "ultimaPagos",
                resultadoPagos.isLast()
        );

        m.addAttribute(
                "inicioPagos",
                pagPagos.inicio()
        );

        m.addAttribute(
                "finPagos",
                pagPagos.fin()
        );

        m.addAttribute(
                "puntosIzquierdaPagos",
                pagPagos.puntosIzquierda()
        );

        m.addAttribute(
                "puntosDerechaPagos",
                pagPagos.puntosDerecha()
        );


        // =============================================
        // CAJA
        // =============================================

        m.addAttribute(
                "hayCajaAbierta",
                caja.existeCajaAbierta()
        );


        return "pagos/lista";
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


        // Hasta 10 páginas mostramos todas.
        if (totalPaginas <= MAX_NUMEROS_PAGINACION) {

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

    @GetMapping("/factura/{idFactura}")
    public String registrarPago(
            @PathVariable Long idFactura,
            Model m
    ) {

        Factura f =
                factura.obtenerPorId(idFactura);

        Asociado asociado =
                factura.obtenerAsociadoFactura(
                        idFactura
                );

        BigDecimal saldo =
                detalle.obtenerSaldoPendienteFactura(
                        idFactura
                );

        boolean hayCajaAbierta =
                caja.existeCajaAbierta();


        m.addAttribute(
                "factura",
                f
        );

        m.addAttribute(
                "asociado",
                asociado
        );

        m.addAttribute(
                "saldo",
                saldo
        );

        m.addAttribute(
                "hayCajaAbierta",
                hayCajaAbierta
        );

        if (hayCajaAbierta) {

            m.addAttribute(
                    "cajasAbiertas",
                    caja.listarAbiertas()
            );
        }


        return "pagos/detalle";
    }


    @PostMapping("/factura/{idFactura}/aplicar")
    public String aplicar(
            @PathVariable Long idFactura,
            @RequestParam Long idUsuario,
            @RequestParam String metodo,
            @RequestParam(required = false)
            String operacion,
            @RequestParam BigDecimal monto,
            @RequestParam(required = false)
            String observaciones,
            RedirectAttributes redirect
    ) {

        try {

            Pago pagoRegistrado = pago.registrarPagoFactura(
                    idFactura,
                    idUsuario,
                    monto,
                    metodo,
                    operacion,
                    observaciones
            );

            auditoria.registrarEvento(
                    "CREAR",
                    "PAGO",
                    pagoRegistrado.getIdPago(),
                    "Pago registrado para la factura ID: "
                            + idFactura
                            + ". Método: "
                            + metodo
                            + ". Monto: "
                            + monto
            );

            redirect.addFlashAttribute(
                    "success",
                    "Pago registrado correctamente y agregado como ingreso a la caja."
            );


            return "redirect:/pagos";


        } catch (BusinessException e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );


            return "redirect:/pagos/factura/"
                    + idFactura;
        }
    }


    @GetMapping("/{id}")
    public String detallePago(
            @PathVariable Long id,
            Model m
    ) {

        m.addAttribute(
                "pago",
                pago.obtenerPorId(id)
        );

        m.addAttribute(
                "detalles",
                detalle.listarPorPago(id)
        );

        return "pagos/pago-detalle";
    }
}