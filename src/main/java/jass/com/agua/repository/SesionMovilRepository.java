package jass.com.agua.repository;

import jass.com.agua.entity.SesionMovil;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface SesionMovilRepository extends JpaRepository<SesionMovil, Long> {
    Optional<SesionMovil> findFirstByTokenHashAndRevocadoFalseAndFechaExpiracionAfter(
            String tokenHash, LocalDateTime ahora);
}
