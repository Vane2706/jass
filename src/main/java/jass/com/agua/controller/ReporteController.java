package jass.com.agua.controller;

import jass.com.agua.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {
    private final ReporteService service;

    @GetMapping("/{tipo}/excel")
    public ResponseEntity<byte[]> excel(@PathVariable String tipo) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-" + tipo + ".xlsx").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(service.generarExcel(tipo));
    }

    @GetMapping("/{tipo}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable String tipo) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-" + tipo + ".pdf").contentType(MediaType.APPLICATION_PDF).body(service.generarPdf(tipo));
    }
}
