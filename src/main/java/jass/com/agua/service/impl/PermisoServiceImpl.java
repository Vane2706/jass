package jass.com.agua.service.impl;

import jass.com.agua.entity.Permiso;

import jass.com.agua.exception.BusinessException;
import jass.com.agua.exception.ResourceNotFoundException;
import jass.com.agua.repository.PermisoRepository;
import jass.com.agua.service.PermisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PermisoServiceImpl implements PermisoService {

    private final PermisoRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<Permiso> listarTodos() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Permiso obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Permiso no encontrado: " + id
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Permiso obtenerPorNombre(String nombre) {
        return repository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Permiso no encontrado: " + nombre
                        )
                );
    }

    @Override
    public Permiso guardar(Permiso entidad) {

        if (entidad == null ||
                entidad.getNombre() == null ||
                entidad.getNombre().isBlank()) {

            throw new BusinessException(
                    "El nombre del permiso es obligatorio."
            );
        }

        entidad.setNombre(
                entidad.getNombre().trim().toUpperCase()
        );

        if (entidad.getModulo() != null) {
            entidad.setModulo(
                    entidad.getModulo().trim().toUpperCase()
            );
        }

        return repository.save(entidad);
    }

    @Override
    public void eliminar(Long id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Permiso no encontrado: " + id
            );
        }

        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeNombre(String nombre) {

        return nombre != null
                && !nombre.isBlank()
                && repository.existsByNombreIgnoreCase(
                nombre.trim()
        );
    }
}
