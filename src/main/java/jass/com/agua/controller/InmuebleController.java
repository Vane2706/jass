package jass.com.agua.controller;

import jass.com.agua.entity.Asociado;
import jass.com.agua.entity.Inmueble;
import jass.com.agua.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/inmuebles")
@RequiredArgsConstructor
public class InmuebleController {

    private final InmuebleService service;
    private final TitularidadService titularidad;
    private final AsociadoService asociado;
    private final ServicioService servicios;

    private static final int TAMANIO_PAGINA = 5;

    @GetMapping
    public String listar(
            @RequestParam(
                    required = false,
                    defaultValue = ""
            ) String q,

            @RequestParam(
                    defaultValue = "0"
            ) int pagina,

            Model m
    ) {

        if (pagina < 0) {
            pagina = 0;
        }

        Page<Inmueble> resultado =
                service.listarPaginado(
                        q,
                        pagina,
                        TAMANIO_PAGINA
                );

        if (resultado.getTotalPages() > 0
                && pagina >= resultado.getTotalPages()) {

            pagina =
                    resultado.getTotalPages() - 1;

            resultado =
                    service.listarPaginado(
                            q,
                            pagina,
                            TAMANIO_PAGINA
                    );
        }


        // =====================================================
        // INMUEBLES DE LA PÁGINA ACTUAL
        // =====================================================

        List<Inmueble> inmuebles =
                resultado.getContent();


        // =====================================================
        // IDS DE LOS INMUEBLES
        // =====================================================

        List<Long> idsInmuebles =
                inmuebles
                        .stream()
                        .map(Inmueble::getIdInmueble)
                        .filter(Objects::nonNull)
                        .toList();


        // =====================================================
        // TITULAR ACTUAL DE CADA INMUEBLE
        //
        // Llave   = idInmueble
        // Valor   = Asociado titular
        // =====================================================

        Map<Long, Asociado> titularesPorInmueble =
                titularidad
                        .obtenerTitularesActivosPorInmuebles(
                                idsInmuebles
                        );


        // =====================================================
        // MODELO
        // =====================================================

        m.addAttribute(
                "inmuebles",
                inmuebles
        );

        m.addAttribute(
                "titularesPorInmueble",
                titularesPorInmueble
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
                "q",
                q
        );

        return "inmuebles/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {

        m.addAttribute(
                "inmueble",
                new Inmueble()
        );

        return "inmuebles/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Inmueble i
    ) {

        return "redirect:/inmuebles/"
                + service.registrar(i)
                .getIdInmueble();
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Long id,
            Model m
    ) {

        m.addAttribute(
                "inmueble",
                service.obtenerPorId(id)
        );

        m.addAttribute(
                "titularidades",
                titularidad.listarPorInmueble(id)
        );

        m.addAttribute(
                "servicios",
                servicios.listarPorInmueble(id)
        );

        return "inmuebles/detalle";
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable Long id,
            Model m
    ) {

        m.addAttribute(
                "inmueble",
                service.obtenerPorId(id)
        );

        return "inmuebles/formulario";
    }

    @PostMapping("/{id}/actualizar")
    public String actualizar(
            @PathVariable Long id,
            @ModelAttribute Inmueble i
    ) {

        service.actualizar(id, i);

        return "redirect:/inmuebles/" + id;
    }

    @PostMapping("/{id}/estado")
    public String estado(
            @PathVariable Long id,
            @RequestParam String estado
    ) {

        service.cambiarEstado(id, estado);

        return "redirect:/inmuebles/" + id;
    }

    @GetMapping("/{id}/titular")
    public String titular(
            @PathVariable Long id,
            Model m
    ) {

        m.addAttribute(
                "inmueble",
                service.obtenerPorId(id)
        );

        return "inmuebles/titular";
    }

    @PostMapping("/{id}/titular")
    public String asignar(
            @PathVariable Long id,

            @RequestParam Long idAsociado,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fecha,

            @RequestParam(required = false)
            String motivo,

            @RequestParam(required = false)
            String observaciones
    ) {

        titularidad.registrar(
                idAsociado,
                id,
                fecha,
                motivo,
                observaciones
        );

        return "redirect:/inmuebles/" + id;
    }

    @PostMapping("/{id}/cambiar-titular")
    public String cambiar(
            @PathVariable Long id,

            @RequestParam Long idAsociado,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fecha,

            @RequestParam String motivo,

            @RequestParam(required = false)
            String observaciones
    ) {

        titularidad.cambiarTitular(
                id,
                idAsociado,
                fecha,
                motivo,
                observaciones
        );

        return "redirect:/inmuebles/" + id;
    }

    @GetMapping("/buscar-asociados")
    @ResponseBody
    public List<Map<String, Object>> buscarAsociados(
            @RequestParam(defaultValue = "") String q
    ) {

        String texto = q.trim();

        if (texto.length() < 2) {
            return List.of();
        }

        Pageable limite =
                PageRequest.of(
                        0,
                        8
                );

        return asociado
                .buscarActivosParaTitular(
                        texto,
                        limite
                )
                .stream()
                .map(a -> {

                    Map<String, Object> resultado =
                            new LinkedHashMap<>();

                    resultado.put(
                            "idAsociado",
                            a.getIdAsociado()
                    );

                    resultado.put(
                            "codigo",
                            a.getCodigoAsociado()
                    );

                    resultado.put(
                            "documento",
                            a.getDocumento()
                    );

                    resultado.put(
                            "nombre",
                            nombreCompleto(a)
                    );

                    return resultado;
                })
                .toList();
    }


    private String nombreCompleto(
            Asociado a
    ) {

        return Stream.of(
                        a.getNombres(),
                        a.getApellidoPaterno(),
                        a.getApellidoMaterno()
                )
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(
                        Collectors.joining(" ")
                );
    }

    @PostMapping("/{idInmueble}/titularidad/{idTitularidad}/finalizar")
    public String finalizarTitularidad(
            @PathVariable Long idInmueble,
            @PathVariable Long idTitularidad
    ) {

        titularidad.finalizarTitularidad(
                idTitularidad,
                LocalDate.now(),
                "CAMBIO DE TITULAR",
                null
        );

        return "redirect:/inmuebles/" + idInmueble;
    }
}