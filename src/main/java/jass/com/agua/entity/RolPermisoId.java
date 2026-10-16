package jass.com.agua.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RolPermisoId implements Serializable {

    @Column(name = "id_rol")
    private Long idRol;

    @Column(name = "id_permiso")
    private Long idPermiso;
}
