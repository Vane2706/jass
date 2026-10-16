package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "MEDIDORES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medidor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medidor")
    private Long idMedidor;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;
    @Column(name = "numero_medidor", length = 50, unique = true)
    private String numeroMedidor;
    @Column(name = "marca", length = 80)
    private String marca;
    @Column(name = "modelo", length = 80)
    private String modelo;
    @Column(name = "fecha_instalacion")
    private LocalDate fechaInstalacion;
    @Column(name = "lectura_inicial")
    private BigDecimal lecturaInicial;
    @Column(name = "estado", length = 20)
    private String estado;
}
