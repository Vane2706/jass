package jass.com.agua.service.impl;

import jass.com.agua.entity.CierreCaja;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.CierreCajaRepository;
import jass.com.agua.service.CierreCajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CierreCajaServiceImpl implements CierreCajaService {
    private final CierreCajaRepository repo;

    public List<CierreCaja> listarTodos() {
        return repo.findAll();
    }

    public CierreCaja obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cierre no encontrado: " + id));
    }

    public List<CierreCaja> listarPorCaja(Long id) {
        return repo.findByCaja_IdCajaOrderByFechaCierreDesc(id);
    }
}
