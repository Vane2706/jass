package jass.com.agua.controller.dashboard;

import jass.com.agua.dto.dashboard.DashboardDTO;
import jass.com.agua.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Year;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;


    @GetMapping("/")
    public String dashboard(

            @RequestParam(
                    required = false
            )
            Integer anio,

            @RequestParam(
                    required = false
            )
            Integer mes,

            Model model
    ) {

        if (anio == null) {
            anio = Year.now().getValue();
        }


        DashboardDTO dashboard =
                dashboardService
                        .obtenerDashboard(
                                anio,
                                mes
                        );


        model.addAttribute(
                "dashboard",
                dashboard
        );


        /*
         * Mostrar años alrededor del actual.
         *
         * Puedes cambiarlo posteriormente por los
         * años existentes realmente en PERIODOS.
         */
        model.addAttribute(
                "anios",
                IntStream.rangeClosed(
                                anio - 4,
                                anio + 1
                        )
                        .boxed()
                        .sorted(
                                (a, b) ->
                                        Integer.compare(
                                                b,
                                                a
                                        )
                        )
                        .toList()
        );

        model.addAttribute(
                "evolucion",
                dashboard.getEvolucionMensual()
        );

        model.addAttribute(
                "consumoMensual",
                dashboard.getConsumoMensual()
        );

        model.addAttribute(
                "consumoTipo",
                dashboard.getConsumoPorTipo()
        );

        model.addAttribute(
                "morosidadSector",
                dashboard.getMorosidadPorSector()
        );

        model.addAttribute(
                "recaudacionMetodo",
                dashboard.getRecaudacionPorMetodo()
        );


        return "dashboard/index";
    }
}
