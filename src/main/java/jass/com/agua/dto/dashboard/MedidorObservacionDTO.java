package jass.com.agua.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedidorObservacionDTO {

    private Long idLectura;

    private Long idMedidor;

    private String numeroMedidor;

    private String asociado;

    private String sector;

    private String observacion;

    private String estado;
}