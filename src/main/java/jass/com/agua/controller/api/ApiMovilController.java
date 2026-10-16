package jass.com.agua.controller.api;

import jass.com.agua.dto.api.*;
import jass.com.agua.entity.Usuario;
import jass.com.agua.repository.UsuarioRepository;
import jass.com.agua.repository.UsuarioRolRepository;
import jass.com.agua.service.AsignacionOperadorService;
import jass.com.agua.service.api.LecturaMovilService;
import jass.com.agua.service.api.SesionMovilService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.DisabledException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movil")
@RequiredArgsConstructor
public class ApiMovilController {
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final SesionMovilService sesionService;
    private final LecturaMovilService lecturaMovilService;
    private final AsignacionOperadorService asignacionOperadorService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginMovilRequest request) {
        if (request == null || request.username() == null || request.password() == null)
            return ResponseEntity.badRequest().body(new MensajeApiDTO("Usuario y contraseña son obligatorios."));
        try {
            Authentication auth = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(request.username().trim(), request.password()));
            Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(auth.getName()).orElseThrow();
            boolean operador = usuarioRolRepository.buscarRolesConRol(usuario.getIdUsuario()).stream().anyMatch(ur -> "OPERADOR".equalsIgnoreCase(ur.getRol().getNombre()));
            if (!operador)
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MensajeApiDTO("La aplicación móvil está disponible únicamente para usuarios con rol OPERADOR."));
            var token = sesionService.crear(usuario);
            return ResponseEntity.ok(new LoginMovilResponse(token.token(), "Bearer", token.expiracion(), new UsuarioMovilDTO(usuario.getIdUsuario(), usuario.getUsername(), usuario.getNombres(), usuario.getApellidos(), "OPERADOR")));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MensajeApiDTO("Usuario o contraseña incorrectos."));
        } catch (DisabledException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MensajeApiDTO("El usuario está inactivo."));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MensajeApiDTO("No se pudo iniciar sesión. Verifica tus credenciales."));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        Usuario u = usuarioRepository.findByUsernameIgnoreCase(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(new UsuarioMovilDTO(u.getIdUsuario(), u.getUsername(), u.getNombres(), u.getApellidos(), "OPERADOR"));
    }

    @GetMapping("/medidores")
    public List<MedidorMovilDTO> medidores(
            Authentication authentication,
            @RequestParam(defaultValue = "") String buscar) {

        Usuario u = usuarioRepository
                .findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow();

        asignacionOperadorService.sincronizarAsignacionesAutomaticas();

        return lecturaMovilService.listarMedidores(
                u.getIdUsuario(),
                buscar
        );
    }

    @PostMapping("/lecturas")
    public ResponseEntity<?> registrar(Authentication authentication, @RequestBody LecturaMovilRequest request) {
        Usuario u = usuarioRepository.findByUsernameIgnoreCase(authentication.getName()).orElseThrow();
        try {
            var lectura = lecturaMovilService.registrar(u.getIdUsuario(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new SincronizacionMovilResponse(request.clientId(), "SINCRONIZADA", "Lectura sincronizada correctamente.", lectura.getIdLectura()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(new MensajeApiDTO(ex.getMessage()));
        } catch (jass.com.agua.exception.BusinessException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MensajeApiDTO(ex.getMessage()));
        }
    }

    @PostMapping("/lecturas/sincronizar")
    public List<SincronizacionMovilResponse> sincronizar(Authentication authentication, @RequestBody List<LecturaMovilRequest> solicitudes) {
        Usuario u = usuarioRepository.findByUsernameIgnoreCase(authentication.getName()).orElseThrow();
        if (solicitudes == null) return List.of();
        return solicitudes.stream().map(req -> {
            try {
                var lectura = lecturaMovilService.registrar(u.getIdUsuario(), req);
                return new SincronizacionMovilResponse(req == null ? null : req.clientId(), "SINCRONIZADA", "Lectura sincronizada correctamente.", lectura.getIdLectura());
            } catch (Exception ex) {
                String mensaje = ex.getMessage() == null ? "No se pudo sincronizar la lectura." : ex.getMessage();
                return new SincronizacionMovilResponse(req == null ? null : req.clientId(), "ERROR", mensaje, null);
            }
        }).toList();
    }

    @PostMapping("/logout")
    public ResponseEntity<MensajeApiDTO> logout(@RequestHeader(value = "Authorization", required = false) String header) {
        if (header != null && header.startsWith("Bearer ")) sesionService.revocar(header.substring(7).trim());
        return ResponseEntity.ok(new MensajeApiDTO("Sesión cerrada."));
    }
}
