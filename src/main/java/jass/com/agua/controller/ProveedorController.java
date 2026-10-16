package jass.com.agua.controller;

import jass.com.agua.entity.*;
import jass.com.agua.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/proveedores")
@RequiredArgsConstructor
public class ProveedorController {
    private final ProveedorService proveedor;
    private final RequerimientoService requerimiento;
    private final DetalleRequerimientoService detalle;
    private final ComprobanteService comprobante;
    private final MaterialService material;
    private final UsuarioService usuario;

    @GetMapping
    public String listar(Model m) {
        m.addAttribute("proveedores", proveedor.listarTodos());
        return "proveedores/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {
        m.addAttribute("proveedor", new Proveedor());
        return "proveedores/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Proveedor p) {
        proveedor.registrar(p);
        return "redirect:/proveedores";
    }

    @GetMapping("/{id}")
    public String detalleProv(@PathVariable Long id, Model m) {
        m.addAttribute("proveedor", proveedor.obtenerPorId(id));
        return "proveedores/detalle";
    }

    @GetMapping("/requerimientos")
    public String reqs(Model m) {
        m.addAttribute("requerimientos", requerimiento.listarTodos());
        m.addAttribute("proveedores", proveedor.listarActivos());
        m.addAttribute("usuarios", usuario.listarActivos());
        return "proveedores/requerimientos";
    }

    @PostMapping("/requerimientos")
    public String req(@RequestParam Long idProveedor, @RequestParam Long idUsuario, @RequestParam String descripcion) {
        var r = requerimiento.registrar(idProveedor, idUsuario, descripcion);
        return "redirect:/proveedores/requerimientos/" + r.getIdRequerimiento();
    }

    @GetMapping("/requerimientos/{id}")
    public String reqDetalle(@PathVariable Long id, Model m) {
        m.addAttribute("requerimiento", requerimiento.obtenerPorId(id));
        m.addAttribute("detalles", detalle.listarPorRequerimiento(id));
        m.addAttribute("comprobantes", comprobante.listarPorRequerimiento(id));
        m.addAttribute("materiales", material.listarActivos());
        return "proveedores/requerimiento-detalle";
    }

    @PostMapping("/requerimientos/{id}/detalle")
    public String add(@PathVariable Long id, @RequestParam Long idMaterial, @RequestParam BigDecimal cantidad, @RequestParam BigDecimal precioUnitario) {
        detalle.agregar(id, idMaterial, cantidad, precioUnitario);
        return "redirect:/proveedores/requerimientos/" + id;
    }

    @PostMapping("/requerimientos/{id}/comprobante")
    public String comp(@PathVariable Long id, @RequestParam String tipo, @RequestParam(required = false) String serie, @RequestParam String numero, @RequestParam(required = false) LocalDate fecha, @RequestParam BigDecimal importe, @RequestParam(required = false) String estadoPago, @RequestParam(required = false) String archivoUrl) {
        comprobante.registrar(id, tipo, serie, numero, fecha, importe, estadoPago, archivoUrl);
        return "redirect:/proveedores/requerimientos/" + id;
    }
}
