package jass.com.agua.repository;

import jass.com.agua.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    Optional<Proveedor> findByRuc(String ruc);

    boolean existsByRuc(String ruc);

    List<Proveedor> findByEstadoIgnoreCaseOrderByRazonSocialAsc(String estado);
}