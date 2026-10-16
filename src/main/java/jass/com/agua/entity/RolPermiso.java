package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ROLPERMISO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolPermiso {

    @EmbeddedId
    private RolPermisoId id;

    @MapsId("idRol")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @MapsId("idPermiso")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_permiso", nullable = false)
    private Permiso permiso;
}
