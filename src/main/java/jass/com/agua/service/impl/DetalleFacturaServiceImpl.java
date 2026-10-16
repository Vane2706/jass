package jass.com.agua.service.impl;

import jass.com.agua.entity.DetalleFactura;
import jass.com.agua.entity.Factura;
import jass.com.agua.entity.Lectura;
import jass.com.agua.entity.Tarifa;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.DetalleFacturaRepository;
import jass.com.agua.repository.FacturaRepository;
import jass.com.agua.repository.LecturaRepository;
import jass.com.agua.repository.TarifaRepository;
import jass.com.agua.service.DetalleFacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DetalleFacturaServiceImpl implements DetalleFacturaService {

    private final DetalleFacturaRepository detalleFacturaRepository;
    private final FacturaRepository facturaRepository;
    private final LecturaRepository lecturaRepository;
    private final TarifaRepository tarifaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DetalleFactura> listarTodos() {
        return detalleFacturaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleFactura obtenerPorId(Long id) {
        return detalleFacturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle de factura no encontrado: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleFactura> listarPorFactura(Long idFactura) {
        return detalleFacturaRepository.findByFactura_IdFacturaOrderByIdDetalleAsc(idFactura);
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleFactura obtenerPorLectura(Long idLectura) {
        return detalleFacturaRepository.findFirstByLectura_IdLecturaOrderByIdDetalleDesc(idLectura)
                .orElseThrow(() -> new ResourceNotFoundException("No existe detalle para la lectura: " + idLectura));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleFactura> listarPorTarifa(Long idTarifa) {
        return detalleFacturaRepository.findByTarifa_IdTarifaOrderByIdDetalleDesc(idTarifa);
    }

    @Override
    public DetalleFactura generarDetalle(Long idFactura, Long idLectura, Long idTarifa) {
        if (detalleFacturaRepository.existsByLectura_IdLectura(idLectura))
            throw new BusinessException("La lectura ya fue facturada.");

        Factura factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada: " + idFactura));
        Lectura lectura = lecturaRepository.findById(idLectura)
                .orElseThrow(() -> new ResourceNotFoundException("Lectura no encontrada: " + idLectura));
        Tarifa tarifa = tarifaRepository.findById(idTarifa)
                .orElseThrow(() -> new ResourceNotFoundException("Tarifa no encontrada: " + idTarifa));

        BigDecimal consumo = lectura.getConsumo() != null ? lectura.getConsumo() : BigDecimal.ZERO;
        BigDecimal importeFijo = tarifa.getImporteFijo() != null ? tarifa.getImporteFijo() : BigDecimal.ZERO;
        BigDecimal importeConsumo = tarifa.getPrecioM3() != null
                ? consumo.multiply(tarifa.getPrecioM3())
                : BigDecimal.ZERO;
        BigDecimal importeTotal = importeFijo.add(importeConsumo);

        DetalleFactura detalle = DetalleFactura.builder()
                .factura(factura)
                .lectura(lectura)
                .tarifa(tarifa)
                .consumo(consumo)
                .importeFijo(importeFijo)
                .importeConsumo(importeConsumo)
                .importeTotal(importeTotal)
                .build();

        return detalleFacturaRepository.save(detalle);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeDetalleParaLectura(Long idLectura) {
        return detalleFacturaRepository.existsByLectura_IdLectura(idLectura);
    }
}
