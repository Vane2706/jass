package jass.com.agua.controller;

import jass.com.agua.entity.Material;
import jass.com.agua.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/inventario")
@RequiredArgsConstructor
public class InventarioController {
    private final MaterialService material;
    private final MovimientoInventarioService movimiento;
    private final UsuarioService usuario;

    @GetMapping
    public String listar(Model m) {
        m.addAttribute("materiales", material.listarTodos());
        return "inventario/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {
        m.addAttribute("material", new Material());
        return "inventario/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Material x) {
        material.registrar(x);
        return "redirect:/inventario";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model m) {
        m.addAttribute("material", material.obtenerPorId(id));
        m.addAttribute("movimientos", movimiento.listarPorMaterial(id));
        m.addAttribute("usuarios", usuario.listarActivos());
        return "inventario/detalle";
    }

    @PostMapping("/{id}/movimiento")
    public String mover(@PathVariable Long id, @RequestParam Long idUsuario, @RequestParam(required = false) Long idRequerimiento, @RequestParam(required = false) Long idComprobante, @RequestParam String tipo, @RequestParam BigDecimal cantidad, @RequestParam(required = false) String motivo, @RequestParam(required = false) String observaciones) {
        movimiento.registrar(id, idUsuario, idRequerimiento, idComprobante, tipo, cantidad, motivo, observaciones);
        return "redirect:/inventario/" + id;
    }
}
