package jass.com.agua.dto.api;
import java.time.LocalDateTime;
public record LoginMovilResponse(String token, String tokenType, LocalDateTime expiresAt, UsuarioMovilDTO usuario) {}
