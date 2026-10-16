package jass.com.agua.service;

import jass.com.agua.entity.CierreCaja;

import java.util.List;

public interface CierreCajaService {
    List<CierreCaja> listarTodos();

    CierreCaja obtenerPorId(Long id);

    List<CierreCaja> listarPorCaja(Long idCaja);
}