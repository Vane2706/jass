package jass.com.agua.controller;

import jass.com.agua.entity.*;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/morosidad")
@RequiredArgsConstructor
public class MorosidadController {
    private final OrdenCorteService orden;
    private final SuspensionService suspension;
    private final ReconexionService reconexion;
    private final ServicioService servicio;
    private final UsuarioService usuario;
    private final TitularidadService titularidad;
    private static final int TAMANIO_PAGINA = 5;
    private static final int MAX_NUMEROS_PAGINACION = 10;

    @GetMapping
    public String inicio(
            @RequestParam(defaultValue = "0") int pagina,
            Model m
    ) {

        // =====================================================
        // EVITAR PÁGINAS NEGATIVAS
        // =====================================================

        pagina =
                Math.max(
                        pagina,
                        0
                );


        // =====================================================
        // ÓRDENES PAGINADAS
        // =====================================================

        Page<OrdenCorte> resultado =
                orden.listarPaginado(
                        pagina,
                        TAMANIO_PAGINA
                );


        // =====================================================
        // CORREGIR PÁGINA SI YA NO EXISTE
        // =====================================================

        if (resultado.getTotalPages() > 0
                && pagina >= resultado.getTotalPages()) {

            pagina =
                    resultado.getTotalPages() - 1;

            resultado =
                    orden.listarPaginado(
                            pagina,
                            TAMANIO_PAGINA
                    );
        }


        // =====================================================
        // ÓRDENES DE LA PÁGINA ACTUAL
        // =====================================================

        List<OrdenCorte> ordenes =
                resultado.getContent();


        // =====================================================
        // OBTENER IDS DE LOS INMUEBLES
        //
        // OrdenCorte
        //      -> Servicio
        //      -> Inmueble
        // =====================================================

        List<Long> idsInmuebles =
                ordenes
                        .stream()

                        .map(OrdenCorte::getServicio)
                        .filter(Objects::nonNull)

                        .map(Servicio::getInmueble)
                        .filter(Objects::nonNull)

                        .map(Inmueble::getIdInmueble)
                        .filter(Objects::nonNull)

                        .distinct()

                        .toList();


        // =====================================================
        // TITULAR ACTIVO DE CADA INMUEBLE
        //
        // Key   -> idInmueble
        // Value -> Asociado
        // =====================================================

        Map<Long, Asociado> titularesPorInmueble =
                titularidad
                        .obtenerTitularesActivosPorInmuebles(
                                idsInmuebles
                        );


        // =====================================================
        // PAGINACIÓN
        // =====================================================

        int paginaActual =
                resultado.getNumber();

        int totalPaginas =
                resultado.getTotalPages();

        PaginacionVista paginacion =
                calcularPaginacion(
                        paginaActual,
                        totalPaginas
                );


        // =====================================================
        // MODELO
        // =====================================================

        m.addAttribute(
                "ordenes",
                ordenes
        );

        m.addAttribute(
                "titularesPorInmueble",
                titularesPorInmueble
        );

        m.addAttribute(
                "paginaActual",
                paginaActual
        );

        m.addAttribute(
                "totalPaginas",
                totalPaginas
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


        return "morosidad/lista";
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

    @PostMapping("/evaluar/{idServicio}")
    public String evaluar(@PathVariable Long idServicio, @RequestParam(required = false) String observaciones) {
        orden.evaluarYGenerar(idServicio, observaciones);
        return "redirect:/morosidad";
    }

    @GetMapping("/orden/{id}")
    public String orden(@PathVariable Long id, Model m) {
        m.addAttribute("orden", orden.obtenerPorId(id));
        return "morosidad/orden";
    }

    @PostMapping("/orden/{id:\\d+}/suspender")
    public String suspender(
            @PathVariable Long id,

            @RequestParam(required = false)
            String motivo,

            @RequestParam(required = false)
            String observaciones,

            RedirectAttributes redirect
    ) {

        suspension.ejecutar(
                id,
                motivo,
                observaciones
        );


        redirect.addFlashAttribute(
                "success",
                "El corte del servicio fue ejecutado correctamente."
        );


        return "redirect:/morosidad";
    }

    @GetMapping("/suspension/{id}")
    public String suspension(@PathVariable Long id, Model m) {
        m.addAttribute("suspension", suspension.obtenerPorId(id));
        return "morosidad/suspension";
    }

    @PostMapping("/suspension/{id}/reconexion")
    public String solicitar(@PathVariable Long id, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha, @RequestParam(required = false) String observaciones) {
        var r = reconexion.solicitar(id, fecha, observaciones);
        return "redirect:/morosidad/reconexion/" + r.getIdReconexion();
    }

    @GetMapping("/reconexion/{id}")
    public String reconexion(@PathVariable Long id, Model m) {
        m.addAttribute("reconexion", reconexion.obtenerPorId(id));
        return "morosidad/reconexion";
    }

    @PostMapping("/reconexion/{id:\\d+}/pagar")
    public String pagarReconexion(
            @PathVariable Long id,

            @RequestParam String metodoPago,

            @RequestParam(required = false)
            String numeroOperacion,

            @RequestParam(required = false)
            String observaciones,

            Authentication authentication,

            RedirectAttributes redirect
    ) {

        Usuario usuarioActual =
                usuario.obtenerPorUsername(
                        authentication.getName()
                );


        reconexion.pagarDeudaYReconexion(
                id,
                usuarioActual.getIdUsuario(),
                metodoPago,
                numeroOperacion,
                observaciones
        );


        redirect.addFlashAttribute(
                "success",
                "Pago registrado correctamente. "
                        + "La deuda y el derecho de reconexión fueron cancelados."
        );


        return "redirect:/morosidad/reconexion/" + id;
    }

    @PostMapping("/reconexion/{id:\\d+}/ejecutar")
    public String ejecutar(
            @PathVariable Long id,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fecha,

            RedirectAttributes redirect
    ) {

        reconexion.ejecutar(
                id,
                fecha
        );


        redirect.addFlashAttribute(
                "success",
                "El servicio fue reconectado correctamente."
        );


        return "redirect:/morosidad";
    }

    @GetMapping("/detalle/{id:\\d+}")
    public String detalle(
            @PathVariable Long id
    ) {

        OrdenCorte ordenCorte =
                orden.obtenerPorId(id);


        String estadoOrden =
                ordenCorte.getEstado() == null
                        ? ""
                        : ordenCorte
                        .getEstado()
                        .trim()
                        .toUpperCase();
        if ("PENDIENTE".equals(estadoOrden)) {

            return "redirect:/morosidad/orden/"
                    + ordenCorte.getIdOrdenCorte();
        }

        if ("EJECUTADA".equals(estadoOrden)) {

            Suspension s =
                    suspension
                            .buscarPorOrdenCorte(
                                    ordenCorte.getIdOrdenCorte()
                            )
                            .orElseThrow(
                                    () -> new BusinessException(
                                            "La orden figura como ejecutada, "
                                                    + "pero no existe una suspensión asociada."
                                    )
                            );

            Optional<Reconexion> reconexionExistente =
                    reconexion.buscarPorSuspension(
                            s.getIdSuspension()
                    );


            if (reconexionExistente.isPresent()) {

                Reconexion r =
                        reconexionExistente.get();


                String estadoReconexion =
                        r.getEstado() == null
                                ? ""
                                : r.getEstado()
                                .trim()
                                .toUpperCase();

                if (!"EJECUTADA".equals(estadoReconexion)) {

                    return "redirect:/morosidad/reconexion/"
                            + r.getIdReconexion();
                }

                return "redirect:/morosidad";
            }
            return "redirect:/morosidad/suspension/"
                    + s.getIdSuspension();
        }

        return "redirect:/morosidad";
    }
}
