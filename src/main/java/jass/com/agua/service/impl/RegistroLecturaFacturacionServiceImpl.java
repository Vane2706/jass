package jass.com.agua.service.impl;

import jass.com.agua.entity.*;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistroLecturaFacturacionServiceImpl
        implements RegistroLecturaFacturacionService {

    private final LecturaService lecturaService;
    private final FacturaService facturaService;

    @Override
    public Lectura registrarYFacturar(
            Long idUsuario,
            Long idMedidor,
            BigDecimal lecturaActual,
            String observaciones,
            String origen
    ) {

        Lectura lectura =
                lecturaService.registrarLectura(
                        idUsuario,
                        idMedidor,
                        lecturaActual,
                        observaciones,
                        origen
                );

        if (lectura.getMedidor() == null ||
                lectura.getMedidor().getServicio() == null ||
                lectura.getMedidor()
                        .getServicio()
                        .getIdServicio() == null) {

            throw new BusinessException(
                    "El medidor registrado no está asociado a un servicio."
            );
        }

        Long idServicio =
                lectura.getMedidor()
                        .getServicio()
                        .getIdServicio();

        Long idPeriodo =
                lectura.getPeriodo()
                        .getIdPeriodo();

        facturaService.generarFactura(
                idServicio,
                idPeriodo
        );

        return lectura;
    }
}