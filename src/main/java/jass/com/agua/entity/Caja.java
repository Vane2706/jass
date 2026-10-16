package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CAJAS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_caja")
    private Long idCaja;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    @Column(name = "nombre", length = 100)
    private String nombre;
    @Column(name = "fecha_apertura")
    private LocalDateTime fechaApertura;
    @Column(name = "saldo_inicial")
    private BigDecimal saldoInicial;
    @Column(name = "saldo_final")
    private BigDecimal saldoFinal;
    @Column(name = "estado", length = 20)
    private String estado;
}
