package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "DETALLEFACTURAS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleFactura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_factura", nullable = false)
    private Factura factura;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_lectura", nullable = false)
    private Lectura lectura;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tarifa", nullable = false)
    private Tarifa tarifa;
    @Column(name = "consumo")
    private BigDecimal consumo;
    @Column(name = "importe_fijo")
    private BigDecimal importeFijo;
    @Column(name = "importe_consumo")
    private BigDecimal importeConsumo;
    @Column(name = "importe_total")
    private BigDecimal importeTotal;
}
