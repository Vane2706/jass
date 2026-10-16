package jass.com.agua.controller;

import jass.com.agua.entity.TipoServicio;
import jass.com.agua.service.TipoServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tipos-servicio")
@RequiredArgsConstructor
public class CatalogoController {
    private final TipoServicioService service;

    @GetMapping
    public String listar(Model m) {
        m.addAttribute("tipos", service.listarTodos());
        m.addAttribute("tipoServicio", new TipoServicio());
        return "tipos-servicio/lista";
    }

    @PostMapping
    public String guardar(@ModelAttribute TipoServicio t) {
        service.registrar(t);
        return "redirect:/tipos-servicio";
    }

    @PostMapping("/{id}/actualizar")
    public String actualizar(@PathVariable Long id, @ModelAttribute TipoServicio t) {
        service.actualizar(id, t);
        return "redirect:/tipos-servicio";
    }

    @PostMapping("/{id}/estado")
    public String estado(@PathVariable Long id, @RequestParam String estado) {
        service.cambiarEstado(id, estado);
        return "redirect:/tipos-servicio";
    }
}
