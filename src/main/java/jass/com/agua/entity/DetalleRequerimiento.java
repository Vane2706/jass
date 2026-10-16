package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "DETALLEREQUERIMIENTOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleRequerimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_requerimiento")
    private Long idDetalleRequerimiento;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_requerimiento", nullable = false)
    private Requerimiento requerimiento;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_material", nullable = false)
    private Material material;
    @Column(name = "cantidad")
    private BigDecimal cantidad;
    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;
    @Column(name = "subtotal")
    private BigDecimal subtotal;
}
