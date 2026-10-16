package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "RECONEXIONES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reconexion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reconexion")
    private Long idReconexion;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_suspension", nullable = false)
    private Suspension suspension;
    @Column(name = "fecha_solicitud")
    private LocalDate fechaSolicitud;
    @Column(name = "fecha_reconexion")
    private LocalDate fechaReconexion;
    @Column(name = "deuda_pendiente")
    private BigDecimal deudaPendiente;
    @Column(name = "monto_reconexion")
    private BigDecimal montoReconexion;
    @Column(name = "monto_total_requerido")
    private BigDecimal montoTotalRequerido;
    @Column(name = "estado_pago", length = 30)
    private String estadoPago;
    @Column(name = "estado", length = 30)
    private String estado;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
}
