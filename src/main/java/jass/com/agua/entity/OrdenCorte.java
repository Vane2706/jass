package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ORDENESCORTE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCorte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_corte")
    private Long idOrdenCorte;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;
    @Column(name = "fecha_generacion")
    private LocalDate fechaGeneracion;
    @Column(name = "motivo", length = 200)
    private String motivo;
    @Column(name = "deuda_acumulada")
    private BigDecimal deudaAcumulada;
    @Column(name = "periodos_pendientes")
    private Integer periodosPendientes;
    @Column(name = "fecha_corte", length = 80)
    private String fechaCorte;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @Column(name = "estado", length = 20)
    private String estado;
}
