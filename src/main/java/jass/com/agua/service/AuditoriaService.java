package jass.com.agua.service;

import jass.com.agua.entity.Auditoria;
import java.util.List;

public interface AuditoriaService {

    List<Auditoria> listarTodos();

    Auditoria obtenerPorId(Long id);

    Auditoria guardar(Auditoria entidad);

    void eliminar(Long id);

    void registrarEvento(
            String accion,
            String tablaAfectada,
            Long registroId,
            String descripcion
    );
}
