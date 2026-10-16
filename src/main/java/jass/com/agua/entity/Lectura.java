package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LECTURAS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lectura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lectura")
    private Long idLectura;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_medidor", nullable = false)
    private Medidor medidor;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_periodo", nullable = false)
    private Periodo periodo;
    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;
    @Column(name = "lectura_anterior")
    private BigDecimal lecturaAnterior;
    @Column(name = "lectura_actual")
    private BigDecimal lecturaActual;
    @Column(name = "consumo")
    private BigDecimal consumo;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @Column(name = "origen", length = 20)
    private String origen;
    @Column(name = "estado_sincronizacion", length = 20)
    private String estadoSincronizacion;
    @Column(name = "fecha_sincronizacion")
    private LocalDateTime fechaSincronizacion;
    @Column(name = "id_captura_movil", length = 100, unique = true)
    private String idCapturaMovil;
}
