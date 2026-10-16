package jass.com.agua.config;

import jass.com.agua.service.api.SesionMovilService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FiltroTokenMovil extends OncePerRequestFilter {
    private final SesionMovilService sesionMovilService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith(request.getContextPath() + "/api/movil/")
                || path.equals(request.getContextPath() + "/api/movil/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            noAutorizado(response, "Falta el token de sesión."); return;
        }
        String token = header.substring(7).trim();
        var username = sesionMovilService.validarYObtenerUsername(token);
        if (username.isEmpty()) {
            noAutorizado(response, "La sesión venció o ya no tiene permiso de operador. Inicia sesión nuevamente."); return;
        }
        var auth = UsernamePasswordAuthenticationToken.authenticated(
                username.get(), null, java.util.List.of(new SimpleGrantedAuthority("ROLE_OPERADOR")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        chain.doFilter(request, response);
    }

    private void noAutorizado(HttpServletResponse response, String mensaje) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String seguro = mensaje.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"mensaje\":\"" + seguro + "\"}");
    }
}
