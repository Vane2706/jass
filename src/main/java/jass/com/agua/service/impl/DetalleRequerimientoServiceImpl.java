package jass.com.agua.service.impl;

import jass.com.agua.entity.*;
import jass.com.agua.exception.*;
import jass.com.agua.repository.*;
import jass.com.agua.service.DetalleRequerimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DetalleRequerimientoServiceImpl implements DetalleRequerimientoService {
    private final DetalleRequerimientoRepository repo;
    private final RequerimientoRepository reqRepo;
    private final MaterialRepository matRepo;

    public List<DetalleRequerimiento> listarTodos() {
        return repo.findAll();
    }

    public DetalleRequerimiento obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Detalle no encontrado: " + id));
    }

    public List<DetalleRequerimiento> listarPorRequerimiento(Long id) {
        return repo.findByRequerimiento_IdRequerimientoOrderByIdDetalleRequerimientoAsc(id);
    }

    public DetalleRequerimiento agregar(Long idReq, Long idMat, BigDecimal cant, BigDecimal precio) {
        if (cant == null || precio == null || cant.signum() <= 0 || precio.signum() < 0)
            throw new BusinessException("Cantidad/precio inválidos.");
        Requerimiento r = reqRepo.findById(idReq).orElseThrow(() -> new ResourceNotFoundException("Requerimiento no encontrado"));
        Material m = matRepo.findById(idMat).orElseThrow(() -> new ResourceNotFoundException("Material no encontrado"));
        DetalleRequerimiento d = repo.save(DetalleRequerimiento.builder().requerimiento(r).material(m).cantidad(cant).precioUnitario(precio).subtotal(cant.multiply(precio)).build());
        recalcular(r);
        return d;
    }

    public void eliminar(Long id) {
        DetalleRequerimiento d = obtenerPorId(id);
        Requerimiento r = d.getRequerimiento();
        repo.delete(d);
        repo.flush();
        recalcular(r);
    }

    private void recalcular(Requerimiento r) {
        BigDecimal t = repo.findByRequerimiento_IdRequerimientoOrderByIdDetalleRequerimientoAsc(r.getIdRequerimiento()).stream().map(DetalleRequerimiento::getSubtotal).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        r.setImporteTotal(t);
        reqRepo.save(r);
    }
}
