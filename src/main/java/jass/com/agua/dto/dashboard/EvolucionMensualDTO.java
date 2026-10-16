package jass.com.agua.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class EvolucionMensualDTO {

    private Integer mes;
    private String nombreMes;

    private BigDecimal facturacion;
    private BigDecimal recaudacion;
}