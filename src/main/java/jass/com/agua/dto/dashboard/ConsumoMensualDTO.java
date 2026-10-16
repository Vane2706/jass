package jass.com.agua.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ConsumoMensualDTO {

    private Integer mes;
    private String nombreMes;

    private BigDecimal consumo;
}