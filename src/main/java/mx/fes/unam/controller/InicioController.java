package mx.fes.unam.controller;

import lombok.RequiredArgsConstructor;
import mx.fes.unam.api.client.AuthApiClient;
import mx.fes.unam.api.client.BackendApiException;
import mx.fes.unam.api.dto.auth.MeResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class InicioController {

    private final AuthApiClient authApiClient;

    @GetMapping("/")
    public String inicio(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        try {
            MeResponse perfil = authApiClient.me();
            model.addAttribute("perfil", perfil);
            if (perfil != null && perfil.getUsername() != null) {
                model.addAttribute("username", perfil.getUsername());
            }
        } catch (BackendApiException ex) {
            if (ex.getStatus() == 401 || ex.getStatus() == 403) {
                throw ex;
            }
            // Otros errores: mantener la vista con datos básicos de autenticación.
        }
        return "inicio";
    }
}
