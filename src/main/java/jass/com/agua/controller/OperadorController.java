package jass.com.agua.controller;

import jass.com.agua.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/operadores")
@RequiredArgsConstructor
public class OperadorController {
    private final AsignacionOperadorService asignacion;
    private final UsuarioService usuario;
    private final ServicioService servicio;

    @GetMapping
    public String listar(Model m) {
        m.addAttribute("asignaciones", asignacion.listarTodas());
        m.addAttribute("usuarios", usuario.listarActivos());
        m.addAttribute("servicios", servicio.listarPorEstado("ACTIVO"));
        return "operadores/lista";
    }

    @PostMapping("/asignar")
    public String asignar(@RequestParam Long idUsuario, @RequestParam Long idServicio, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        asignacion.asignar(idUsuario, idServicio, inicio, fin);
        return "redirect:/operadores";
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long id, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        asignacion.finalizarAsignacion(id, fin);
        return "redirect:/operadores";
    }
}
