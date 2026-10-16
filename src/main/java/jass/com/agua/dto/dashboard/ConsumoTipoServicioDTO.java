package jass.com.agua.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ConsumoTipoServicioDTO {

    private String tipoServicio;
    private BigDecimal consumo;
}