package mx.fes.unam.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import mx.fes.unam.api.client.AuthApiClient;
import mx.fes.unam.api.client.BackendApiException;
import mx.fes.unam.api.dto.auth.LoginRequest;
import mx.fes.unam.api.dto.auth.LoginResponse;
import mx.fes.unam.api.dto.error.ApiError;
import mx.fes.unam.api.support.ApiSessionKeys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final AuthApiClient authApiClient;
    private final SecurityContextRepository securityContextRepository;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          HttpSession session,
                          Model model) {
        try {
            LoginResponse loginResponse = authApiClient.login(new LoginRequest(username, password));

            session.setAttribute(ApiSessionKeys.ACCESS_TOKEN, loginResponse.getAccessToken());
            session.setAttribute(ApiSessionKeys.TOKEN_EXPIRES_IN, loginResponse.getExpiresIn());

            String rol = loginResponse.getRol() != null ? loginResponse.getRol() : "";
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
            var authentication = new UsernamePasswordAuthenticationToken(loginResponse.getUsername(), null, authorities);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            return "redirect:/";
        } catch (BackendApiException ex) {
            model.addAttribute("loginError", mensajeLogin(ex));
            model.addAttribute("username", username);
            return "login";
        }
    }

    private String mensajeLogin(BackendApiException ex) {
        ApiError apiError = ex.getApiError();
        if (apiError != null && apiError.getMensaje() != null && !apiError.getMensaje().isBlank()) {
            return apiError.getMensaje();
        }
        int status = ex.getStatus();
        if (status == 401) {
            return "Usuario o contraseña incorrectos.";
        }
        if (status == 403) {
            return "Cuenta deshabilitada. No tiene acceso al sistema.";
        }
        return "No fue posible iniciar sesión. Intente nuevamente.";
    }
}
