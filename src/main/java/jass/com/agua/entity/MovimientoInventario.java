package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "MOVIMIENTOINVENTARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long idMovimiento;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_material", nullable = false)
    private Material material;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_requerimiento", nullable = true)
    private Requerimiento requerimiento;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_comprobante", nullable = true)
    private Comprobante comprobante;
    @Column(name = "tipo_movimiento", length = 20)
    private String tipoMovimiento;
    @Column(name = "cantidad")
    private BigDecimal cantidad;
    @Column(name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento;
    @Column(name = "motivo", length = 200)
    private String motivo;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
}
