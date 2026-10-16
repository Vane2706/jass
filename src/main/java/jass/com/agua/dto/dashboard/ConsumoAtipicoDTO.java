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
public class ConsumoAtipicoDTO {

    private Long idLectura;

    private String codigoServicio;

    private String numeroMedidor;

    private String asociado;

    private String sector;

    private BigDecimal consumoActual;

    private BigDecimal promedioHistorico;

    private BigDecimal variacion;

    private String estado;
}