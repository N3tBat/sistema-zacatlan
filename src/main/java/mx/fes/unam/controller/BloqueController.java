package mx.fes.unam.controller;

import lombok.RequiredArgsConstructor;
import mx.fes.unam.api.client.BackendApiException;
import mx.fes.unam.api.client.BloqueApiClient;
import mx.fes.unam.api.dto.bloque.BloqueRequest;
import mx.fes.unam.api.dto.bloque.BloqueResponse;
import mx.fes.unam.api.dto.error.ApiError;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/bloque")
@RequiredArgsConstructor
public class BloqueController {

    private final BloqueApiClient bloqueApiClient;

    @GetMapping("/ver")
    public String ver(Model model) {
        List<BloqueResponse> bloques = bloqueApiClient.listar();
        model.addAttribute("bloques", bloques);
        return "bloque/bloque";
    }

    @GetMapping("/alta")
    public String alta(Model model) {
        BloqueResponse bloque = new BloqueResponse();
        bloque.setActivo(true);
        model.addAttribute("bloque", bloque);
        model.addAttribute("esEdicion", false);
        return "bloque/alta-bloque";
    }

    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Long id, Model model) {
        BloqueResponse bloque = bloqueApiClient.obtener(id);
        model.addAttribute("bloque", bloque);
        model.addAttribute("esEdicion", true);
        return "bloque/alta-bloque";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("bloque") BloqueResponse bloque,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        boolean esEdicion = bloque != null && bloque.getId() != null;
        BloqueRequest request = new BloqueRequest(
                bloque != null ? bloque.getNombre() : null,
                bloque != null ? bloque.getDescripcion() : null,
                bloque != null ? bloque.getActivo() : null
        );

        try {
            if (esEdicion) {
                bloqueApiClient.actualizar(bloque.getId(), request);
                redirectAttributes.addFlashAttribute("exito", "Bloque actualizado correctamente.");
            } else {
                bloqueApiClient.crear(request);
                redirectAttributes.addFlashAttribute("exito", "Bloque creado correctamente.");
            }
            return "redirect:/bloque/ver";
        } catch (BackendApiException ex) {
            if (ex.getStatus() == 400) {
                aplicarErroresValidacion(ex, bindingResult, model);
                model.addAttribute("esEdicion", esEdicion);
                return "bloque/alta-bloque";
            }
            throw ex;
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        bloqueApiClient.eliminar(id);
        redirectAttributes.addFlashAttribute("exito", "Bloque eliminado correctamente.");
        return "redirect:/bloque/ver";
    }

    private void aplicarErroresValidacion(BackendApiException ex, BindingResult bindingResult, Model model) {
        ApiError apiError = ex.getApiError();
        String mensaje = (apiError != null && apiError.getMensaje() != null && !apiError.getMensaje().isBlank())
                ? apiError.getMensaje()
                : "La petición contiene datos inválidos.";
        model.addAttribute("error", mensaje);

        if (apiError != null) {
            Map<String, String> detalles = apiError.getDetalles();
            if (detalles != null && !detalles.isEmpty()) {
                model.addAttribute("errorDetalles", detalles);
                for (Map.Entry<String, String> entry : detalles.entrySet()) {
                    String field = entry.getKey();
                    String msg = entry.getValue() != null ? entry.getValue() : mensaje;
                    if (field != null && bindingResult.getTarget() != null) {
                        try {
                            bindingResult.rejectValue(field, "backend", msg);
                        } catch (Exception ignored) {
                            // si el campo no existe en el objeto del formulario, se muestra en errorDetalles
                        }
                    }
                }
            }
        }
    }
}
