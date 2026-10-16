package jass.com.agua.service;

import jass.com.agua.entity.Lectura;

import java.math.BigDecimal;

public interface RegistroLecturaFacturacionService {

    Lectura registrarYFacturar(
            Long idUsuario,
            Long idMedidor,
            BigDecimal lecturaActual,
            String observaciones,
            String origen
    );
}