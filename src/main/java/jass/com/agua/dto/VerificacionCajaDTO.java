package jass.com.agua.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class VerificacionCajaDTO {

    private boolean autorizado;

    private String mensaje;

    private BigDecimal saldoEsperado;
}