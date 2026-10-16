package jass.com.agua.service.impl;

import jass.com.agua.entity.Material;
import jass.com.agua.exception.*;
import jass.com.agua.repository.MaterialRepository;
import jass.com.agua.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MaterialServiceImpl implements MaterialService {
    private final MaterialRepository repo;

    public List<Material> listarTodos() {
        return repo.findAll();
    }

    public Material obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Material no encontrado: " + id));
    }

    public List<Material> listarActivos() {
        return repo.findByEstadoIgnoreCaseOrderByNombreAsc("ACTIVO");
    }

    public Material registrar(Material m) {
        if (m.getCodigo() == null || m.getCodigo().isBlank()) throw new BusinessException("Código obligatorio.");
        if (repo.existsByCodigo(m.getCodigo())) throw new BusinessException("Código de material duplicado.");
        m.setIdMaterial(null);
        if (m.getStockActual() == null) m.setStockActual(BigDecimal.ZERO);
        if (m.getStockMinimo() == null) m.setStockMinimo(BigDecimal.ZERO);
        if (m.getEstado() == null) m.setEstado("ACTIVO");
        return repo.save(m);
    }

    public Material actualizar(Long id, Material d) {
        Material m = obtenerPorId(id);
        m.setNombre(d.getNombre());
        m.setDescripcion(d.getDescripcion());
        m.setUnidadMedida(d.getUnidadMedida());
        m.setStockMinimo(d.getStockMinimo());
        return repo.save(m);
    }

    public Material cambiarEstado(Long id, String e) {
        Material m = obtenerPorId(id);
        m.setEstado(e.toUpperCase());
        return repo.save(m);
    }
}
