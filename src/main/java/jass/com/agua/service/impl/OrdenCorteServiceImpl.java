package jass.com.agua.service.impl;

import jass.com.agua.entity.*;
import jass.com.agua.exception.*;
import jass.com.agua.repository.*;
import jass.com.agua.service.OrdenCorteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrdenCorteServiceImpl implements OrdenCorteService {
    private final OrdenCorteRepository ordenRepo;
    private final ServicioRepository servicioRepo;
    private final FacturaRepository facturaRepo;
    private final DetallePagoRepository detallePagoRepo;

    public List<OrdenCorte> listarTodos() {
        return ordenRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdenCorte> listarPaginado(
            int pagina,
            int tamanio
    ) {

        int paginaSegura =
                Math.max(
                        pagina,
                        0
                );

        Pageable pageable =
                PageRequest.of(
                        paginaSegura,
                        tamanio,
                        Sort.by(
                                Sort.Order.desc("fechaGeneracion"),
                                Sort.Order.desc("idOrdenCorte")
                        )
                );

        return ordenRepo.findAll(
                pageable
        );
    }

    public OrdenCorte obtenerPorId(Long id) {
        return ordenRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Orden de corte no encontrada: " + id));
    }

    public List<OrdenCorte> listarPorServicio(Long id) {
        return ordenRepo.findByServicio_IdServicioOrderByFechaGeneracionDesc(id);
    }

    public List<OrdenCorte> listarPorEstado(String e) {
        return ordenRepo.findByEstadoIgnoreCaseOrderByFechaGeneracionDesc(e);
    }

    public OrdenCorte evaluarYGenerar(Long idServicio, String obs) {
        Servicio s = servicioRepo.findById(idServicio).orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + idServicio));
        if (ordenRepo.findFirstByServicio_IdServicioAndEstadoIgnoreCaseOrderByFechaGeneracionDesc(idServicio, "PENDIENTE").isPresent())
            throw new BusinessException("El servicio ya tiene una orden de corte pendiente.");
        List<Factura> deudas = facturaRepo.findByServicio_IdServicioAndEstadoInOrderByFechaVencimientoAsc(idServicio, List.of("PENDIENTE", "PARCIAL"));
        deudas = deudas.stream().filter(f -> f.getFechaVencimiento() == null || !f.getFechaVencimiento().isAfter(LocalDate.now())).toList();
        if (deudas.size() < 2)
            throw new BusinessException("Se requieren al menos 2 periodos pendientes vencidos para generar la orden de corte.");
        BigDecimal deuda = BigDecimal.ZERO;
        for (Factura f : deudas) {
            BigDecimal pag = detallePagoRepo.sumarMontoAplicadoPorFactura(f.getIdFactura());
            if (pag == null) pag = BigDecimal.ZERO;
            deuda = deuda.add((f.getTotal() == null ? BigDecimal.ZERO : f.getTotal()).subtract(pag).max(BigDecimal.ZERO));
        }
        OrdenCorte o = OrdenCorte.builder().servicio(s).fechaGeneracion(LocalDate.now()).motivo("MOROSIDAD - 2 O MÁS PERIODOS PENDIENTES").deudaAcumulada(deuda).periodosPendientes(deudas.size()).observaciones(obs).estado("PENDIENTE").build();
        return ordenRepo.save(o);
    }

    public OrdenCorte actualizarEstado(Long id, String estado, String fechaCorte) {
        OrdenCorte o = obtenerPorId(id);
        o.setEstado(estado.trim().toUpperCase());
        if (fechaCorte != null && !fechaCorte.isBlank()) o.setFechaCorte(fechaCorte);
        return ordenRepo.save(o);
    }
}