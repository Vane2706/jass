package jass.com.agua.repository;

import jass.com.agua.entity.TipoServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoServicioRepository extends JpaRepository<TipoServicio, Long> {
    Optional<TipoServicio> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    List<TipoServicio> findByEstadoIgnoreCaseOrderByNombreAsc(String estado);
}
