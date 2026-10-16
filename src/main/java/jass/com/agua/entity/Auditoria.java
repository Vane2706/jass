package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "AUDITORIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    @Column(name = "accion", length = 100)
    private String accion;
    @Column(name = "tabla_afectada", length = 100)
    private String tablaAfectada;
    @Column(name = "registro_id")
    private Long registroId;
    @Column(name = "fecha")
    private LocalDateTime fecha;
    @Column(name = "ip", length = 50)
    private String ip;
    @Lob
    @Column(name = "descripcion")
    private String descripcion;
}
