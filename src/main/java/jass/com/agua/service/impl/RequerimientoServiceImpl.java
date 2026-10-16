package jass.com.agua.service.impl;

import jass.com.agua.entity.*;
import jass.com.agua.exception.*;
import jass.com.agua.repository.*;
import jass.com.agua.service.RequerimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RequerimientoServiceImpl implements RequerimientoService {
    private final RequerimientoRepository repo;
    private final ProveedorRepository provRepo;
    private final UsuarioRepository usrRepo;

    public List<Requerimiento> listarTodos() {
        return repo.findAll();
    }

    public Requerimiento obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Requerimiento no encontrado: " + id));
    }

    public List<Requerimiento> listarPorEstado(String e) {
        return repo.findByEstadoIgnoreCaseOrderByFechaRequerimientoDesc(e);
    }

    public Requerimiento registrar(Long p, Long u, String desc) {
        Proveedor pr = provRepo.findById(p).orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
        Usuario us = usrRepo.findById(u).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return repo.save(Requerimiento.builder().proveedor(pr).usuario(us).fechaRequerimiento(LocalDate.now()).descripcion(desc).importeTotal(BigDecimal.ZERO).estado("REGISTRADO").build());
    }

    public Requerimiento actualizarEstado(Long id, String e) {
        Requerimiento r = obtenerPorId(id);
        r.setEstado(e.toUpperCase());
        return repo.save(r);
    }
}
