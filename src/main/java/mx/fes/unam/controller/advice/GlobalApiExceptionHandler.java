package mx.fes.unam.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mx.fes.unam.api.client.BackendApiException;
import mx.fes.unam.api.dto.error.ApiError;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(annotations = Controller.class)
public class GlobalApiExceptionHandler {

    @ExceptionHandler(BackendApiException.class)
    public String handleBackendApiException(BackendApiException ex,
                                            HttpServletRequest request,
                                            RedirectAttributes redirectAttributes) {
        int status = ex.getStatus();

        if (status == 401) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            redirectAttributes.addAttribute("expired", "true");
            return "redirect:/login";
        }

        if (status == 403) {
            return "redirect:/acceso-denegado";
        }

        ApiError apiError = ex.getApiError();
        String mensaje = (apiError != null && apiError.getMensaje() != null && !apiError.getMensaje().isBlank())
                ? apiError.getMensaje()
                : "Ocurrió un error al procesar la solicitud.";
        redirectAttributes.addFlashAttribute("error", mensaje);
        if (apiError != null && apiError.getDetalles() != null && !apiError.getDetalles().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorDetalles", apiError.getDetalles());
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            // Evitar redirecciones externas simples: usar ruta relativa si es posible
            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            String prefix = request.getScheme() + "://" + request.getServerName();
            int port = request.getServerPort();
            boolean defaultPort = ("http".equals(request.getScheme()) && port == 80)
                    || ("https".equals(request.getScheme()) && port == 443);
            String origin = defaultPort ? prefix : prefix + ":" + port;
            if (referer.startsWith(origin + contextPath)) {
                String target = referer.substring(origin.length());
                if (!target.isBlank()) {
                    return "redirect:" + target;
                }
            }
        }
        return "redirect:/";
    }
}