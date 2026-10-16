package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "TIPOSERVICIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_servicio")
    private Long idTipoServicio;
    @Column(
            name = "nombre",
            length = 50,
            nullable = false,
            unique = true
    )
    private String nombre;

    @Column(
            name = "descripcion",
            length = 200,
            nullable = false
    )
    private String descripcion;

    @Column(
            name = "estado",
            length = 20,
            nullable = false
    )
    private String estado;
}
