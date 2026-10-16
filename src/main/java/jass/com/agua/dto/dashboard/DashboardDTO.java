package jass.com.agua.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    private Integer anio;
    private Integer mes;

    private BigDecimal totalFacturado;
    private BigDecimal consumoTotal;
    private BigDecimal totalRecaudado;
    private BigDecimal indiceRecaudacion;
    private BigDecimal totalMorosidad;
    private Long serviciosAptosCorte;

    private List<EvolucionMensualDTO> evolucionMensual;

    private List<ConsumoMensualDTO> consumoMensual;

    private List<ConsumoTipoServicioDTO> consumoPorTipo;

    private List<MorosidadSectorDTO> morosidadPorSector;

    private List<RecaudacionMetodoDTO> recaudacionPorMetodo;

    private List<ServicioVencidoDTO> serviciosVencidos;

    private List<ConsumoAtipicoDTO> consumosAtipicos;

    private List<MedidorObservacionDTO> medidoresObservacion;

    private EfectividadCortesDTO efectividadCortes;
}