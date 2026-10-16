package jass.com.agua.repository;

import jass.com.agua.entity.Auditoria;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaRepository
        extends JpaRepository<Auditoria, Long> {

    @EntityGraph(attributePaths = "usuario")
    List<Auditoria> findAllByOrderByFechaDesc();

    @EntityGraph(attributePaths = "usuario")
    List<Auditoria> findByUsuario_IdUsuarioOrderByFechaDesc(
            Long idUsuario
    );

    @EntityGraph(attributePaths = "usuario")
    List<Auditoria> findByFechaBetweenOrderByFechaDesc(
            LocalDateTime desde,
            LocalDateTime hasta
    );
}