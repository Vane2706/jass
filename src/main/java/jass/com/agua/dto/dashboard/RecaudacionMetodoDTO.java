package jass.com.agua.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RecaudacionMetodoDTO {

    private String metodo;
    private BigDecimal monto;
}