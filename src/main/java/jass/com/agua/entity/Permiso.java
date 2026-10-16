package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "PERMISOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permiso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Long idPermiso;
    @Column(name = "nombre", length = 100)
    private String nombre;
    @Column(name = "modulo", length = 80)
    private String modulo;
    @Column(name = "descripcion", length = 200)
    private String descripcion;
}
