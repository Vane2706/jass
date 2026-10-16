package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "INMUEBLES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inmueble {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inmueble")
    private Long idInmueble;
    @Column(name = "codigo_inmueble", length = 20, unique = true)
    private String codigoInmueble;
    @Column(name = "direccion", length = 200)
    private String direccion;
    @Column(name = "referencia", length = 200)
    private String referencia;
    @Column(name = "distrito", length = 100)
    private String distrito;
    @Column(name = "sector", length = 100)
    private String sector;
    @Column(name = "numero_lote", length = 30)
    private String numeroLote;
    @Column(name = "manzana", length = 30)
    private String manzana;
    @Column(name = "estado", length = 20)
    private String estado;
}
