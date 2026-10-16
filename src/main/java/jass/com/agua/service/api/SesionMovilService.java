package jass.com.agua.service.api;

import jass.com.agua.entity.SesionMovil;
import jass.com.agua.entity.Usuario;
import jass.com.agua.repository.SesionMovilRepository;
import jass.com.agua.repository.UsuarioRolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SesionMovilService {
    private final SesionMovilRepository sesiones;
    private final UsuarioRolRepository usuarioRolRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    public record TokenCreado(String token, LocalDateTime expiracion) {}

    @Transactional
    public TokenCreado crear(Usuario usuario) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime expiracion = ahora.plusDays(30);
        sesiones.save(SesionMovil.builder().usuario(usuario).tokenHash(hash(token))
                .fechaCreacion(ahora).fechaExpiracion(expiracion).revocado(false).build());
        return new TokenCreado(token, expiracion);
    }

    @Transactional(readOnly = true)
    public Optional<String> validarYObtenerUsername(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return sesiones.findFirstByTokenHashAndRevocadoFalseAndFechaExpiracionAfter(hash(token), LocalDateTime.now())
                .filter(s -> "ACTIVO".equalsIgnoreCase(s.getUsuario().getEstado()))
                .filter(s -> usuarioRolRepository.buscarRolesConRol(s.getUsuario().getIdUsuario()).stream()
                        .anyMatch(ur -> "OPERADOR".equalsIgnoreCase(ur.getRol().getNombre())))
                .map(s -> s.getUsuario().getUsername());
    }

    @Transactional
    public void revocar(String token) {
        if (token == null || token.isBlank()) return;
        sesiones.findFirstByTokenHashAndRevocadoFalseAndFechaExpiracionAfter(hash(token), LocalDateTime.now())
                .ifPresent(s -> { s.setRevocado(true); sesiones.save(s); });
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) { throw new IllegalStateException("No se pudo validar la sesión móvil", e); }
    }
}
