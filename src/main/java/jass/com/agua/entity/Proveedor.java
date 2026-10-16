package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "PROVEEDORES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Long idProveedor;
    @Column(name = "razon_social", length = 150)
    private String razonSocial;
    @Column(name = "ruc", length = 20)
    private String ruc;
    @Column(name = "telefono", length = 30)
    private String telefono;
    @Column(name = "correo", length = 120)
    private String correo;
    @Column(name = "direccion", length = 200)
    private String direccion;
    @Column(name = "estado", length = 20)
    private String estado;
}
