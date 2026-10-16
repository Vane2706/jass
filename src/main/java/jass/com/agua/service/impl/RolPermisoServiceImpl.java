package jass.com.agua.service.impl;

import jass.com.agua.entity.*;
import jass.com.agua.exception.*;
import jass.com.agua.repository.*;
import jass.com.agua.service.RolPermisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RolPermisoServiceImpl implements RolPermisoService {
    private final RolPermisoRepository repo;
    private final RolRepository rol;
    private final PermisoRepository per;

    public List<RolPermiso> listarTodos() {
        return repo.findAll();
    }

    public List<RolPermiso> listarPorRol(Long id) {
        return repo.findByRol_IdRol(id);
    }

    public RolPermiso asignar(Long r, Long p) {
        RolPermisoId id = new RolPermisoId(r, p);
        if (repo.existsById(id)) throw new BusinessException("El rol ya tiene ese permiso.");
        return repo.save(RolPermiso.builder().id(id).rol(rol.findById(r).orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"))).permiso(per.findById(p).orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado"))).build());
    }

    public void quitar(Long r, Long p) {
        repo.deleteById(new RolPermisoId(r, p));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tienePermiso(
            Long idRol,
            Long idPermiso
    ) {
        return repo.existsById(
                new RolPermisoId(idRol, idPermiso)
        );
    }
}
