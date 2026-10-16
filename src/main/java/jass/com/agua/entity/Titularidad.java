package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "TITULARIDADES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Titularidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_titularidad")
    private Long idTitularidad;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_asociado", nullable = false)
    private Asociado asociado;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_inmueble", nullable = false)
    private Inmueble inmueble;
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
    @Column(name = "motivo", length = 150)
    private String motivo;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
    @Column(name = "estado", length = 20)
    private String estado;
}
