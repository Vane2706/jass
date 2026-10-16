package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "FACTURAS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Long idFactura;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_periodo", nullable = false)
    private Periodo periodo;
    @Column(name = "numero_factura", length = 30, unique = true)
    private String numeroFactura;
    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;
    @Column(name = "subtotal")
    private BigDecimal subtotal;
    @Column(name = "total")
    private BigDecimal total;
    @Column(name = "estado", length = 30)
    private String estado;
}
