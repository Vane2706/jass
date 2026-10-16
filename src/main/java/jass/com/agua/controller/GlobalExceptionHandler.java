package jass.com.agua.controller;

import jass.com.agua.exception.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public String negocio(BusinessException e, Model m) {
        m.addAttribute("titulo", "Operación no permitida");
        m.addAttribute("mensaje", e.getMessage());
        return "error/negocio";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String noEncontrado(ResourceNotFoundException e, Model m) {
        m.addAttribute("titulo", "Registro no encontrado");
        m.addAttribute("mensaje", e.getMessage());
        return "error/no-encontrado";
    }

    @ExceptionHandler(Exception.class)
    public String general(Exception e, Model m) {
        m.addAttribute("titulo", "Error inesperado");
        m.addAttribute("mensaje", e.getMessage());
        return "error/general";
    }
}
