package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "COMPROBANTES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante")
    private Long idComprobante;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_requerimiento", nullable = false)
    private Requerimiento requerimiento;
    @Column(name = "tipo_comprobante", length = 30)
    private String tipoComprobante;
    @Column(name = "serie", length = 20)
    private String serie;
    @Column(name = "numero", length = 30)
    private String numero;
    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;
    @Column(name = "importe")
    private BigDecimal importe;
    @Column(name = "estado_pago", length = 30)
    private String estadoPago;
    @Column(name = "archivo_url", length = 500)
    private String archivoUrl;
}
