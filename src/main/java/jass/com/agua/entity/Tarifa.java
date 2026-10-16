package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "TARIFAS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarifa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarifa")
    private Long idTarifa;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_servicio", nullable = false)
    private TipoServicio tipoServicio;
    @Column(name = "nombre", length = 100)
    private String nombre;
    @Column(name = "consumo_minimo")
    private BigDecimal consumoMinimo;
    @Column(name = "consumo_maximo")
    private BigDecimal consumoMaximo;
    @Column(name = "importe_fijo")
    private BigDecimal importeFijo;
    @Column(name = "precio_m3")
    private BigDecimal precioM3;
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
    @Column(name = "estado", length = 20)
    private String estado;
}
