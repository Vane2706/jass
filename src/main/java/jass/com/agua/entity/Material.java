package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "MATERIALES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material")
    private Long idMaterial;
    @Column(name = "codigo", length = 30)
    private String codigo;
    @Column(name = "nombre", length = 150)
    private String nombre;
    @Lob
    @Column(name = "descripcion")
    private String descripcion;
    @Column(name = "unidad_medida", length = 30)
    private String unidadMedida;
    @Column(name = "stock_actual")
    private BigDecimal stockActual;
    @Column(name = "stock_minimo")
    private BigDecimal stockMinimo;
    @Column(name = "estado", length = 20)
    private String estado;
}
