package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PAGOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_asociado", nullable = false)
    private Asociado asociado;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;
    @Column(name = "monto_total")
    private BigDecimal montoTotal;
    @Column(name = "metodo_pago", length = 30)
    private String metodoPago;
    @Column(name = "numero_operacion", length = 80)
    private String numeroOperacion;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @Column(name = "estado", length = 20)
    private String estado;
}
