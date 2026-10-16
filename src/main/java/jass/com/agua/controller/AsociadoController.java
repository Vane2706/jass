package jass.com.agua.controller;

import jass.com.agua.entity.Asociado;
import jass.com.agua.entity.Pago;
import jass.com.agua.service.AsociadoService;
import jass.com.agua.service.PagoService;
import jass.com.agua.service.TitularidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/asociados")
@RequiredArgsConstructor
public class AsociadoController {

    private final AsociadoService service;
    private final TitularidadService titularidad;
    private final PagoService pagos;
    private static final int TAMANIO_PAGINA = 5;
    private static final int TAMANIO_PAGINA_PAGOS = 5;

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

        Page<Asociado> resultado =
                service.listarPaginado(
                        q,
                        pagina,
                        TAMANIO_PAGINA
                );

        if (resultado.getTotalPages() > 0
                && pagina >= resultado.getTotalPages()) {

            pagina = resultado.getTotalPages() - 1;

            resultado = service.listarPaginado(
                    q,
                    pagina,
                    TAMANIO_PAGINA
            );
        }

        m.addAttribute(
                "asociados",
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
                "q",
                q
        );

        return "asociados/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {

        m.addAttribute(
                "asociado",
                new Asociado()
        );

        return "asociados/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Asociado a,
            RedirectAttributes r
    ) {

        service.registrar(a);

        r.addFlashAttribute(
                "success",
                "Asociado registrado."
        );
        return "redirect:/asociados";
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Long id,

            @RequestParam(
                    defaultValue = "0"
            ) int paginaPagos,

            Model m
    ) {

        if (paginaPagos < 0) {
            paginaPagos = 0;
        }

        Asociado asociado =
                service.obtenerPorId(id);

        Page<Pago> resultadoPagos =
                pagos.listarPorAsociadoPaginado(
                        id,
                        paginaPagos,
                        TAMANIO_PAGINA_PAGOS
                );

        if (resultadoPagos.getTotalPages() > 0
                && paginaPagos >=
                resultadoPagos.getTotalPages()) {

            paginaPagos =
                    resultadoPagos
                            .getTotalPages() - 1;

            resultadoPagos =
                    pagos.listarPorAsociadoPaginado(
                            id,
                            paginaPagos,
                            TAMANIO_PAGINA_PAGOS
                    );
        }

        m.addAttribute(
                "asociado",
                asociado
        );

        m.addAttribute(
                "titularidades",
                titularidad.listarPorAsociado(id)
        );

        m.addAttribute(
                "pagos",
                resultadoPagos.getContent()
        );

        m.addAttribute(
                "paginaPagosActual",
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
                "esPrimeraPaginaPagos",
                resultadoPagos.isFirst()
        );

        m.addAttribute(
                "esUltimaPaginaPagos",
                resultadoPagos.isLast()
        );


        return "asociados/detalle";
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable Long id,
            Model m
    ) {

        m.addAttribute(
                "asociado",
                service.obtenerPorId(id)
        );

        return "asociados/formulario";
    }

    @PostMapping("/{id}/actualizar")
    public String actualizar(
            @PathVariable Long id,
            @ModelAttribute Asociado a
    ) {

        service.actualizar(id, a);

        return "redirect:/asociados/" + id;
    }

    @PostMapping("/{id}/estado")
    public String estado(
            @PathVariable Long id,
            @RequestParam String estado
    ) {

        service.cambiarEstado(id, estado);

        return "redirect:/asociados/" + id;
    }
}