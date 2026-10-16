package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "ROLES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;
    @Column(name = "nombre", length = 50)
    private String nombre;
    @Column(name = "descripcion", length = 100)
    private String descripcion;
}
