package jass.com.agua.service.impl;

import jass.com.agua.entity.*;
import jass.com.agua.exception.*;
import jass.com.agua.repository.*;
import jass.com.agua.service.ComprobanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ComprobanteServiceImpl implements ComprobanteService {
    private final ComprobanteRepository repo;
    private final RequerimientoRepository reqRepo;

    public List<Comprobante> listarTodos() {
        return repo.findAll();
    }

    public Comprobante obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Comprobante no encontrado: " + id));
    }

    public List<Comprobante> listarPorRequerimiento(Long id) {
        return repo.findByRequerimiento_IdRequerimientoOrderByFechaEmisionDesc(id);
    }

    public Comprobante registrar(Long idReq, String tipo, String serie, String numero, LocalDate fecha, BigDecimal importe, String estado, String url) {
        Requerimiento r = reqRepo.findById(idReq).orElseThrow(() -> new ResourceNotFoundException("Requerimiento no encontrado"));
        return repo.save(Comprobante.builder().requerimiento(r).tipoComprobante(tipo).serie(serie).numero(numero).fechaEmision(fecha == null ? LocalDate.now() : fecha).importe(importe).estadoPago(estado == null ? "PENDIENTE" : estado.toUpperCase()).archivoUrl(url).build());
    }

    public Comprobante actualizarEstadoPago(Long id, String e) {
        Comprobante c = obtenerPorId(id);
        c.setEstadoPago(e.toUpperCase());
        return repo.save(c);
    }
}
