package jass.com.agua.controller;

import jass.com.agua.dto.MedidorLecturaDTO;
import jass.com.agua.entity.*;
import jass.com.agua.repository.PeriodoRepository;
import jass.com.agua.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/lecturas")
@RequiredArgsConstructor
public class LecturaController {
    private final LecturaService service;
    private final MedidorService medidor;
    private final PeriodoService periodo;
    private final UsuarioService usuario;
    private final InmuebleService inmueble;
    private final TitularidadService titularidad;
    private final RegistroLecturaFacturacionService registroFacturacion;
    private final AuditoriaService auditoria;
    private static final int TAMANIO_PAGINA = 5;

    @GetMapping
    public String listar(

            @RequestParam(
                    required = false
            ) Long idPeriodo,

            @RequestParam(
                    required = false
            ) Long idInmueble,

            @RequestParam(
                    defaultValue = "0"
            ) int pagina,

            Model m
    ) {

        // =====================================================
        // EVITAR PÁGINAS NEGATIVAS
        // =====================================================

        if (pagina < 0) {
            pagina = 0;
        }


        // =====================================================
        // OBTENER LECTURAS PAGINADAS
        // =====================================================

        Page<Lectura> resultado =
                service.listarPaginado(
                        idPeriodo,
                        idInmueble,
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
                    service.listarPaginado(
                            idPeriodo,
                            idInmueble,
                            pagina,
                            TAMANIO_PAGINA
                    );
        }


        // =====================================================
        // LECTURAS DE LA PÁGINA ACTUAL
        // =====================================================

        List<Lectura> lecturas =
                resultado.getContent();

        List<Long> idsInmuebles =
                lecturas
                        .stream()

                        .map(Lectura::getMedidor)
                        .filter(Objects::nonNull)

                        .map(Medidor::getServicio)
                        .filter(Objects::nonNull)

                        .map(Servicio::getInmueble)
                        .filter(Objects::nonNull)

                        .map(Inmueble::getIdInmueble)
                        .filter(Objects::nonNull)

                        .distinct()

                        .toList();

        Map<Long, Asociado> titularesPorInmueble =
                titularidad
                        .obtenerTitularesActivosPorInmuebles(
                                idsInmuebles
                        );


        // =====================================================
        // MODELO
        // =====================================================

        m.addAttribute(
                "lecturas",
                lecturas
        );

        m.addAttribute(
                "titularesPorInmueble",
                titularesPorInmueble
        );

        m.addAttribute(
                "periodos",
                periodo.listarTodos()
        );

        m.addAttribute(
                "idPeriodo",
                idPeriodo
        );

        m.addAttribute(
                "idInmueble",
                idInmueble
        );

        Inmueble inmuebleSeleccionado = null;

        if (idInmueble != null) {
            inmuebleSeleccionado =
                    inmueble.obtenerPorId(idInmueble);
        }

        m.addAttribute(
                "inmuebleSeleccionado",
                inmuebleSeleccionado
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


        return "lecturas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model m) {

        var operadores =
                usuario.listarOperadoresActivos();

        m.addAttribute(
                "usuarios",
                operadores
        );

        m.addAttribute(
                "periodoActual",
                periodo.obtenerOCrearPeriodoActual()
        );

        return "lecturas/formulario";
    }

    @GetMapping("/buscar-medidores")
    @ResponseBody
    public List<MedidorLecturaDTO> buscarMedidores(
            @RequestParam String q
    ) {
        return medidor.buscarParaLectura(q);
    }

    @PostMapping("/guardar")
    public String guardar(
            @RequestParam Long idUsuario,
            @RequestParam Long idMedidor,
            @RequestParam BigDecimal lecturaActual,
            @RequestParam(required = false) String observaciones
    ) {

        Lectura lectura =
                registroFacturacion.registrarYFacturar(
                        idUsuario,
                        idMedidor,
                        lecturaActual,
                        observaciones,
                        "WEB"
                );

        auditoria.registrarEvento(
                "CREAR",
                "LECTURA",
                lectura.getIdLectura(),
                "Lectura registrada desde la web. "
                        + "Medidor ID: " + idMedidor
        );

        return "redirect:/lecturas/"
                + lectura.getIdLectura();
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model m) {
        m.addAttribute("lectura", service.obtenerPorId(id));
        return "lecturas/detalle";
    }

    @GetMapping("/calcular")
    @ResponseBody
    public BigDecimal calcular(@RequestParam Long idMedidor, @RequestParam BigDecimal lecturaActual) {
        return service.calcularConsumo(idMedidor, lecturaActual);
    }

    @GetMapping("/buscar-direcciones")
    @ResponseBody
    public List<Map<String, Object>> buscarDirecciones(
            @RequestParam(defaultValue = "") String q
    ) {

        String texto = q.trim();

        if (texto.length() < 2) {
            return List.of();
        }

        return inmueble
                .buscarDirecciones(
                        texto,
                        PageRequest.of(0, 8)
                )
                .stream()
                .map(i -> {

                    Map<String, Object> resultado =
                            new LinkedHashMap<>();

                    resultado.put(
                            "idInmueble",
                            i.getIdInmueble()
                    );

                    resultado.put(
                            "direccion",
                            i.getDireccion()
                    );

                    resultado.put(
                            "codigo",
                            i.getCodigoInmueble()
                    );

                    return resultado;
                })
                .toList();
    }
}
