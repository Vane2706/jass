package jass.com.agua.service.impl;

import jass.com.agua.entity.Rol;

import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.RolRepository;
import jass.com.agua.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RolServiceImpl implements RolService {

    private final RolRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<Rol> listarTodos() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Rol obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Rol no encontrado: " + id
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Rol obtenerPorNombre(String nombre) {

        return repository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Rol no encontrado: " + nombre
                        )
                );
    }

    @Override
    public Rol guardar(Rol entidad) {

        if (entidad == null ||
                entidad.getNombre() == null ||
                entidad.getNombre().isBlank()) {

            throw new BusinessException(
                    "El nombre del rol es obligatorio."
            );
        }

        entidad.setNombre(
                entidad.getNombre().trim().toUpperCase()
        );

        return repository.save(entidad);
    }

    @Override
    public void eliminar(Long id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Rol no encontrado: " + id
            );
        }

        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return false;
        }

        return repository.existsByNombreIgnoreCase(
                nombre.trim()
        );
    }
}
