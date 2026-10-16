package jass.com.agua.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EfectividadCortesDTO {

    private Long ordenesGeneradas;

    private Long cortesEjecutados;

    private Long reconexionesRealizadas;

    private Long pendientes;

    private BigDecimal porcentajeEjecucion;

    private BigDecimal porcentajePendiente;
}