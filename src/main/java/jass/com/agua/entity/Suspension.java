package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SUSPENSIONES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suspension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suspension")
    private Long idSuspension;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_orden_corte", nullable = false)
    private OrdenCorte ordenCorte;
    @Column(name = "fecha_suspension")
    private LocalDateTime fechaSuspension;
    @Column(name = "motivo", length = 200)
    private String motivo;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @Column(name = "estado", length = 30)
    private String estado;
}
