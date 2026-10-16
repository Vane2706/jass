package jass.com.agua.controller;

import jass.com.agua.entity.*;
import jass.com.agua.exception.BusinessException;
import jass.com.agua.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/seguridad")
@RequiredArgsConstructor
public class SeguridadController {
    private final UsuarioService usuario;
    private final RolService rol;
    private final PermisoService permiso;
    private final UsuarioRolService usuarioRol;
    private final RolPermisoService rolPermiso;
    private final AuditoriaService auditoria;

    @GetMapping("/usuarios")
    public String usuarios(Model m) {

        m.addAttribute(
                "usuarios",
                usuario.listarTodos()
        );

        m.addAttribute(
                "roles",
                rol.listarTodos()
        );

        m.addAttribute(
                "rolesPorUsuario",
                usuarioRol.obtenerRolesPorUsuario()
        );

        return "seguridad/usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", rol.listarTodos());
        return "seguridad/usuario-formulario";
    }

    @PostMapping("/usuarios")
    public String crear(
            @ModelAttribute Usuario u,
            @RequestParam Long idRol,
            Model model) {

        try {

            Usuario nuevoUsuario =
                    usuario.registrar(u);

            usuarioRol.asignar(
                    nuevoUsuario.getIdUsuario(),
                    idRol
            );

            return "redirect:/login";

        } catch (BusinessException e) {

            /*
             * IMPORTANTE:
             * No devolver la contraseña cifrada ni modificarla.
             * El formulario vuelve a mostrar los demás datos.
             */
            u.setPassword(null);

            model.addAttribute(
                    "usuario",
                    u
            );

            model.addAttribute(
                    "roles",
                    rol.listarTodos()
            );

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "seguridad/usuario-formulario";
        }
    }

    @PostMapping("/usuarios/{id}/estado")
    public String estado(@PathVariable Long id, @RequestParam String estado) {
        usuario.cambiarEstado(id, estado);
        return "redirect:/seguridad/usuarios";
    }

    @PostMapping("/usuarios/{id}/rol")
    public String rol(@PathVariable Long id, @RequestParam Long idRol) {
        usuarioRol.asignar(id, idRol);
        return "redirect:/seguridad/usuarios";
    }

    @PostMapping("/usuarios/{id}/rol/{idRol}/quitar")
    public String quitarRol(@PathVariable Long id, @PathVariable Long idRol) {
        usuarioRol.quitar(id, idRol);
        return "redirect:/seguridad/usuarios";
    }

    @GetMapping("/roles")
    public String roles(Model m) {
        m.addAttribute("roles", rol.listarTodos());
        m.addAttribute("permisos", permiso.listarTodos());
        return "seguridad/roles";
    }

    @PostMapping("/roles")
    public String crearRol(@ModelAttribute Rol r) {
        rol.guardar(r);
        return "redirect:/seguridad/roles";
    }

    @PostMapping("/roles/{id}/permiso")
    public String permiso(@PathVariable Long id, @RequestParam Long idPermiso) {
        rolPermiso.asignar(id, idPermiso);
        return "redirect:/seguridad/roles";
    }

    @GetMapping("/auditoria")
    public String auditoria(Model m) {
        m.addAttribute("auditorias", auditoria.listarTodos());
        return "seguridad/auditoria";
    }
}
