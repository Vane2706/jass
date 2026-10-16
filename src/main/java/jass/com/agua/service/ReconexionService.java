package jass.com.agua.service;

import jass.com.agua.entity.Reconexion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReconexionService {
    List<Reconexion> listarTodos();

    Reconexion obtenerPorId(Long id);

    List<Reconexion> listarPorServicio(Long idServicio);

    Reconexion solicitar(Long idSuspension, LocalDate fechaSolicitud, String observaciones);

    Reconexion marcarPago(Long id, boolean pagado);

    Reconexion ejecutar(Long id, LocalDate fechaReconexion);

    Optional<Reconexion> buscarPorSuspension(
            Long idSuspension
    );

    Reconexion pagarDeudaYReconexion(
            Long idReconexion,
            Long idUsuario,
            String metodoPago,
            String numeroOperacion,
            String observaciones
    );
}