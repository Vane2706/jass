package jass.com.agua.service.impl;

import jass.com.agua.entity.Proveedor;
import jass.com.agua.exception.*;
import jass.com.agua.repository.ProveedorRepository;
import jass.com.agua.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProveedorServiceImpl implements ProveedorService {
    private final ProveedorRepository repo;

    public List<Proveedor> listarTodos() {
        return repo.findAll();
    }

    public Proveedor obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado: " + id));
    }

    public List<Proveedor> listarActivos() {
        return repo.findByEstadoIgnoreCaseOrderByRazonSocialAsc("ACTIVO");
    }

    public Proveedor registrar(Proveedor p) {
        if (p.getRuc() != null && !p.getRuc().isBlank() && repo.existsByRuc(p.getRuc()))
            throw new BusinessException("RUC ya registrado.");
        p.setIdProveedor(null);
        if (p.getEstado() == null) p.setEstado("ACTIVO");
        return repo.save(p);
    }

    public Proveedor actualizar(Long id, Proveedor d) {
        Proveedor p = obtenerPorId(id);
        p.setRazonSocial(d.getRazonSocial());
        p.setRuc(d.getRuc());
        p.setTelefono(d.getTelefono());
        p.setCorreo(d.getCorreo());
        p.setDireccion(d.getDireccion());
        return repo.save(p);
    }

    public Proveedor cambiarEstado(Long id, String e) {
        Proveedor p = obtenerPorId(id);
        p.setEstado(e.toUpperCase());
        return repo.save(p);
    }
}
