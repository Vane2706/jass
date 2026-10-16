package jass.com.agua.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jass.com.agua.entity.Auditoria;

import jass.com.agua.entity.Usuario;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.AuditoriaRepository;
import jass.com.agua.repository.UsuarioRepository;
import jass.com.agua.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository repository;
    private final UsuarioRepository usuarioRepository;

    private static final ZoneId ZONA_LIMA =
            ZoneId.of("America/Lima");

    @Override
    @Transactional(readOnly = true)
    public List<Auditoria> listarTodos() {
        return repository.findAllByOrderByFechaDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Auditoria obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Auditoría no encontrada: " + id
                        )
                );
    }

    @Override
    public Auditoria guardar(Auditoria entidad) {
        return repository.save(entidad);
    }

    @Override
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Auditoría no encontrada: " + id
            );
        }

        repository.deleteById(id);
    }

    @Override
    public void registrarEvento(
            String accion,
            String tablaAfectada,
            Long registroId,
            String descripcion
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        // No registrar usuarios anónimos como si fueran usuarios reales.
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return;
        }

        String username = authentication.getName();

        Usuario usuario = usuarioRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró el usuario autenticado: "
                                        + username
                        )
                );

        String ip = obtenerIpCliente();

        Auditoria evento = Auditoria.builder()
                .usuario(usuario)
                .accion(accion)
                .tablaAfectada(tablaAfectada)
                .registroId(registroId)
                .fecha(LocalDateTime.now(ZONA_LIMA))
                .ip(ip)
                .descripcion(descripcion)
                .build();

        repository.save(evento);
    }

    private String obtenerIpCliente() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return "NO DISPONIBLE";
        }

        HttpServletRequest request = attributes.getRequest();

        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }

        String ip = request.getRemoteAddr();

        if (ip == null || ip.isBlank()) {
            return "NO DISPONIBLE";
        }

        if ("0:0:0:0:0:0:0:1".equals(ip)
                || "::1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }
}
