package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CIERRECAJA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CierreCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cierre")
    private Long idCierre;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_caja", nullable = false)
    private Caja caja;
    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;
    @Column(name = "total_ingresos")
    private BigDecimal totalIngresos;
    @Column(name = "total_egresos")
    private BigDecimal totalEgresos;
    @Column(name = "saldo_calculado")
    private BigDecimal saldoCalculado;
    @Column(name = "saldo_real")
    private BigDecimal saldoReal;
    @Column(name = "diferencia")
    private BigDecimal diferencia;
}
