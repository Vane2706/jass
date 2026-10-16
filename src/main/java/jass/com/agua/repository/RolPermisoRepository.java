package jass.com.agua.repository;

import jass.com.agua.entity.RolPermiso;
import jass.com.agua.entity.RolPermisoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, RolPermisoId> {
    List<RolPermiso> findByRol_IdRol(Long idRol);
}