package jass.com.agua.dto.api;
import java.math.BigDecimal;
public record MedidorMovilDTO(
        Long idMedidor,
        String numeroMedidor,
        String codigoServicio,
        String codigoInmueble,
        String direccion,
        String sector,
        String asociado,
        String codigoAsociado,
        BigDecimal lecturaAnterior,
        String periodo,
        boolean yaRegistrada
) {}
