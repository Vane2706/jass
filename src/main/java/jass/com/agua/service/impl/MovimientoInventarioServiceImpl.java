package jass.com.agua.service.impl;

import jass.com.agua.entity.*;
import jass.com.agua.exception.*;
import jass.com.agua.repository.*;
import jass.com.agua.service.MovimientoInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {
    private final MovimientoInventarioRepository repo;
    private final MaterialRepository materialRepo;
    private final UsuarioRepository usuarioRepo;
    private final RequerimientoRepository reqRepo;
    private final ComprobanteRepository compRepo;

    public List<MovimientoInventario> listarTodos() {
        return repo.findAll();
    }

    public MovimientoInventario obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado: " + id));
    }

    public List<MovimientoInventario> listarPorMaterial(Long id) {
        return repo.findByMaterial_IdMaterialOrderByFechaMovimientoDesc(id);
    }

    public MovimientoInventario registrar(Long idMaterial, Long idUsuario, Long idReq, Long idComp, String tipo, BigDecimal cantidad, String motivo, String obs) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0)
            throw new BusinessException("Cantidad inválida.");
        Material m = materialRepo.findById(idMaterial).orElseThrow(() -> new ResourceNotFoundException("Material no encontrado"));
        Usuario u = usuarioRepo.findById(idUsuario).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        String t = tipo == null ? "" : tipo.toUpperCase();
        BigDecimal stock = m.getStockActual() == null ? BigDecimal.ZERO : m.getStockActual();
        if ("ENTRADA".equals(t)) stock = stock.add(cantidad);
        else if ("SALIDA".equals(t)) {
            if (stock.compareTo(cantidad) < 0) throw new BusinessException("Stock insuficiente.");
            stock = stock.subtract(cantidad);
        } else throw new BusinessException("Tipo debe ser ENTRADA o SALIDA.");
        m.setStockActual(stock);
        materialRepo.save(m);
        Requerimiento r = idReq == null ? null : reqRepo.findById(idReq).orElseThrow(() -> new ResourceNotFoundException("Requerimiento no encontrado"));
        Comprobante c = idComp == null ? null : compRepo.findById(idComp).orElseThrow(() -> new ResourceNotFoundException("Comprobante no encontrado"));
        return repo.save(MovimientoInventario.builder().material(m).usuario(u).requerimiento(r).comprobante(c).tipoMovimiento(t).cantidad(cantidad).fechaMovimiento(LocalDateTime.now()).motivo(motivo).observaciones(obs).build());
    }
}
