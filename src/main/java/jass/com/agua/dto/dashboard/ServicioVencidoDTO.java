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
public class ServicioVencidoDTO {

    private Long idServicio;

    private String codigoServicio;

    private String asociado;

    private String sector;

    private Long periodosVencidos;

    private BigDecimal montoPendiente;

    private String estado;
}