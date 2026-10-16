package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "MOVIMIENTOSCAJA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long idMovimiento;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_caja", nullable = false)
    private Caja caja;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_pago", nullable = true)
    private Pago pago;
    @Column(name = "tipo_movimiento", length = 200)
    private String tipoMovimiento;
    @Column(name = "concepto", length = 200)
    private String concepto;
    @Column(name = "monto")
    private BigDecimal monto;
    @Column(name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
}
