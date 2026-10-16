package jass.com.agua.service.impl;

import jass.com.agua.entity.*;
import jass.com.agua.exception.*;
import jass.com.agua.repository.*;
import jass.com.agua.service.SuspensionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SuspensionServiceImpl implements SuspensionService {
    private final SuspensionRepository repo;
    private final OrdenCorteRepository ordenRepo;
    private final ServicioRepository servicioRepo;

    public List<Suspension> listarTodos() {
        return repo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Suspension> buscarPorOrdenCorte(
            Long idOrdenCorte
    ) {

        return repo.findFirstByOrdenCorte_IdOrdenCorteOrderByFechaSuspensionDesc(idOrdenCorte);
    }

    public Suspension obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Suspensión no encontrada: " + id));
    }

    public List<Suspension> listarPorServicio(Long id) {
        return repo.findByOrdenCorte_Servicio_IdServicioOrderByFechaSuspensionDesc(id);
    }

    public Suspension ejecutar(Long idOrden, String motivo, String obs) {
        OrdenCorte o = ordenRepo.findById(idOrden).orElseThrow(() -> new ResourceNotFoundException("Orden de corte no encontrada: " + idOrden));
        if (!"PENDIENTE".equalsIgnoreCase(o.getEstado()))
            throw new BusinessException("La orden de corte no está pendiente.");
        Servicio s = o.getServicio();
        s.setEstado("SUSPENDIDO");
        servicioRepo.save(s);
        o.setEstado("EJECUTADA");
        o.setFechaCorte(java.time.LocalDate.now().toString());
        ordenRepo.save(o);
        return repo.save(Suspension.builder().ordenCorte(o).fechaSuspension(LocalDateTime.now()).motivo(motivo == null || motivo.isBlank() ? "CORTE POR MOROSIDAD" : motivo).observaciones(obs).estado("ACTIVA").build());
    }

    public Suspension actualizarEstado(Long id, String e) {
        Suspension s = obtenerPorId(id);
        s.setEstado(e.trim().toUpperCase());
        return repo.save(s);
    }
}