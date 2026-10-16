package jass.com.agua.repository;

import jass.com.agua.entity.Usuario;
import jass.com.agua.entity.UsuarioRol;
import jass.com.agua.entity.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {
    List<UsuarioRol> findByUsuario_IdUsuario(Long idUsuario);
    @Query("""
            SELECT ur
            FROM UsuarioRol ur
            JOIN FETCH ur.rol
            WHERE ur.usuario.idUsuario = :idUsuario
            """)
    List<UsuarioRol> buscarRolesConRol(
            @Param("idUsuario") Long idUsuario
    );

    @Query("""
            SELECT DISTINCT ur.usuario
            FROM UsuarioRol ur
            JOIN ur.usuario u
            JOIN ur.rol r
            WHERE UPPER(r.nombre) = 'OPERADOR'
              AND UPPER(u.estado) = 'ACTIVO'
            ORDER BY u.nombres ASC, u.apellidos ASC
            """)
    List<Usuario> buscarUsuariosOperadoresActivos();

    @Query("""
    SELECT ur
    FROM UsuarioRol ur
    JOIN FETCH ur.usuario u
    JOIN FETCH ur.rol r
    """)
    List<UsuarioRol> buscarTodosConUsuarioYRol();
}