package jass.com.agua.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "USUARIOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;
    @Column(name = "username", length = 80, unique = true)
    private String username;
    @Column(name = "password", length = 255)
    private String password;
    @Column(name = "nombres", length = 100)
    private String nombres;
    @Column(name = "apellidos", length = 150)
    private String apellidos;
    @Column(name = "correo", length = 120, unique = true)
    private String correo;
    @Column(name = "estado", length = 20)
    private String estado;
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
