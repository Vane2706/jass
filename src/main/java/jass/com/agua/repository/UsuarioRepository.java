package jass.com.agua.repository;

import jass.com.agua.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsernameIgnoreCase(String username);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByCorreoIgnoreCase(String correo);

    List<Usuario> findByEstadoIgnoreCaseOrderByNombresAsc(String estado);
}