package jass.com.agua.dto.api;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record LecturaMovilRequest(String clientId, Long idMedidor, BigDecimal lecturaActual,
                                  String observaciones, LocalDateTime fechaCaptura) {}
