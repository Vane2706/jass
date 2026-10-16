package jass.com.agua.service.impl;

import jass.com.agua.entity.*;
import jass.com.agua.exception.*;
import jass.com.agua.repository.*;
import jass.com.agua.service.AsignacionOperadorService;
import jass.com.agua.service.UsuarioRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioRolServiceImpl implements UsuarioRolService {
    private final UsuarioRolRepository repo;
    private final UsuarioRepository usr;
    private final RolRepository rol;
    private final AsignacionOperadorService asignacionOperadorService;

    public List<UsuarioRol> listarTodos() {
        return repo.findAll();
    }

    public List<UsuarioRol> listarPorUsuario(Long id) {
        return repo.findByUsuario_IdUsuario(id);
    }


    @Override
    public UsuarioRol asignar(Long u, Long r) {

        UsuarioRolId id = new UsuarioRolId(u, r);

        if (repo.existsById(id)) {
            throw new BusinessException("El usuario ya tiene ese rol.");
        }

        Usuario usuario = usr.findById(u)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado"));

        Rol rolSeleccionado = rol.findById(r)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Rol no encontrado"));

        UsuarioRol guardado = repo.save(
                UsuarioRol.builder()
                        .id(id)
                        .usuario(usuario)
                        .rol(rolSeleccionado)
                        .build()
        );

        if ("OPERADOR".equalsIgnoreCase(rolSeleccionado.getNombre())
                && "ACTIVO".equalsIgnoreCase(usuario.getEstado())) {

            asignacionOperadorService.sincronizarAsignacionesAutomaticas();
        }

        return guardado;
    }


    public void quitar(Long u, Long r) {
        repo.deleteById(new UsuarioRolId(u, r));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> obtenerRolesPorUsuario() {

        return repo
                .buscarTodosConUsuarioYRol()
                .stream()
                .collect(
                        Collectors.toMap(
                                ur -> ur.getUsuario()
                                        .getIdUsuario(),

                                ur -> ur.getRol()
                                        .getIdRol(),

                                (rolExistente, rolNuevo) ->
                                        rolExistente
                        )
                );
    }
}
