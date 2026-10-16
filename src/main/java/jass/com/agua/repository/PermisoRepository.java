package jass.com.agua.repository;

import jass.com.agua.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    List<Permiso> findByModuloIgnoreCaseOrderByNombreAsc(String modulo);
    Optional<Permiso> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}