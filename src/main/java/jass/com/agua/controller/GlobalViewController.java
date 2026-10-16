package jass.com.agua.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalViewController {

    @ModelAttribute("currentUri")
    public String currentUri(
            HttpServletRequest request
    ) {

        return request.getRequestURI();
    }
}