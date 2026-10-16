package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ASOCIADOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asociado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asociado")
    private Long idAsociado;
    @Column(name = "codigo_asociado", length = 20, unique = true)
    private String codigoAsociado;
    @Column(name = "tipo_persona", length = 20)
    private String tipoPersona;
    @Column(name = "nombres", length = 100)
    private String nombres;
    @Column(name = "apellido_paterno", length = 80)
    private String apellidoPaterno;
    @Column(name = "apellido_materno", length = 80)
    private String apellidoMaterno;
    @Column(name = "documento", length = 20, unique = true)
    private String documento;
    @Column(name = "telefono", length = 20)
    private String telefono;
    @Column(name = "correo", length = 120)
    private String correo;
    @Column(name = "direccion_referencia", length = 200)
    private String direccionReferencia;
    @Column(name = "estado", length = 20)
    private String estado;
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
}
