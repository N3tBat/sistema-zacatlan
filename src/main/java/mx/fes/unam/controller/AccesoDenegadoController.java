package mx.fes.unam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccesoDenegadoController {

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "error/acceso-denegado";
    }
}
