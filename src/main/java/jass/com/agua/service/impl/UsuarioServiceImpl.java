package jass.com.agua.service.impl;

import jass.com.agua.entity.Usuario;
import jass.com.agua.exception.*;
import jass.com.agua.repository.UsuarioRepository;
import jass.com.agua.repository.UsuarioRolRepository;
import jass.com.agua.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository repo;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder encoder;

    public List<Usuario> listarTodos() {
        return repo.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    public Usuario obtenerPorUsername(String u) {
        return repo.findByUsernameIgnoreCase(u).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + u));
    }

    public List<Usuario> listarActivos() {
        return repo.findByEstadoIgnoreCaseOrderByNombresAsc("ACTIVO");
    }

    public Usuario registrar(Usuario u) {

        if (u == null) {
            throw new BusinessException(
                    "Los datos del usuario son obligatorios."
            );
        }

        if (u.getUsername() == null ||
                u.getUsername().isBlank()) {

            throw new BusinessException(
                    "El nombre de usuario es obligatorio."
            );
        }

        if (u.getPassword() == null ||
                u.getPassword().isBlank()) {

            throw new BusinessException(
                    "La contraseña es obligatoria."
            );
        }

        if (u.getPassword().length() < 6) {
            throw new BusinessException(
                    "La contraseña debe tener al menos 6 caracteres."
            );
        }

        if (repo.existsByUsernameIgnoreCase(
                u.getUsername().trim())) {

            throw new BusinessException(
                    "El nombre de usuario ya está registrado."
            );
        }

        if (u.getCorreo() != null &&
                !u.getCorreo().isBlank() &&
                repo.existsByCorreoIgnoreCase(
                        u.getCorreo().trim())) {

            throw new BusinessException(
                    "El correo ya está registrado."
            );
        }

        u.setIdUsuario(null);

        u.setUsername(
                u.getUsername().trim()
        );

        if (u.getCorreo() != null) {
            u.setCorreo(
                    u.getCorreo().trim()
            );
        }

        u.setPassword(
                encoder.encode(u.getPassword())
        );

        u.setFechaCreacion(
                LocalDateTime.now()
        );

        u.setEstado("ACTIVO");

        return repo.save(u);
    }

    public Usuario actualizar(Long id, Usuario d) {
        Usuario u = obtenerPorId(id);
        u.setNombres(d.getNombres());
        u.setApellidos(d.getApellidos());
        u.setCorreo(d.getCorreo());
        return repo.save(u);
    }

    public Usuario cambiarEstado(Long id, String e) {
        Usuario u = obtenerPorId(id);
        u.setEstado(e.toUpperCase());
        return repo.save(u);
    }

    public void cambiarPassword(Long id, String p) {
        if (p == null || p.length() < 6) throw new BusinessException("La contraseña debe tener al menos 6 caracteres.");
        Usuario u = obtenerPorId(id);
        u.setPassword(encoder.encode(p));
        repo.save(u);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarOperadoresActivos() {
        return usuarioRolRepository.buscarUsuariosOperadoresActivos();
    }
}
