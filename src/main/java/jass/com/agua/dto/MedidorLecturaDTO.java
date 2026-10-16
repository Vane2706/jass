package jass.com.agua.dto;

public record MedidorLecturaDTO(
        Long idMedidor,
        String numeroMedidor,
        String codigoServicio,
        String codigoInmueble,
        String direccion,
        String referencia,
        String sector,
        String asociado
) {
}
