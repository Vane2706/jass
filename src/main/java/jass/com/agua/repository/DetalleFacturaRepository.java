package jass.com.agua.repository;

import jass.com.agua.entity.DetalleFactura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DetalleFacturaRepository extends JpaRepository<DetalleFactura, Long> {
    List<DetalleFactura> findByFactura_IdFacturaOrderByIdDetalleAsc(Long idFactura);

    Optional<DetalleFactura> findFirstByLectura_IdLecturaOrderByIdDetalleDesc(Long idLectura);

    boolean existsByLectura_IdLectura(Long idLectura);

    List<DetalleFactura> findByTarifa_IdTarifaOrderByIdDetalleDesc(Long idTarifa);
}
