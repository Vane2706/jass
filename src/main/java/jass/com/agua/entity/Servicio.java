package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "SERVICIOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long idServicio;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_inmueble", nullable = false)
    private Inmueble inmueble;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_servicio", nullable = false)
    private TipoServicio tipoServicio;
    @Column(name = "codigo_servicio", length = 20, unique = true)
    private String codigoServicio;
    @Column(name = "fecha_alta")
    private LocalDate fechaAlta;
    @Column(name = "estado", length = 30)
    private String estado;
    @Lob
    @Column(name = "observaciones")
    private String observaciones;
}
