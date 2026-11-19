package mx.fes.unam.controller;

import lombok.RequiredArgsConstructor;
import mx.fes.unam.api.client.BackendApiException;
import mx.fes.unam.api.client.BloqueApiClient;
import mx.fes.unam.api.client.VendedorApiClient;
import mx.fes.unam.api.dto.error.ApiError;
import mx.fes.unam.api.dto.vendedor.VendedorRequest;
import mx.fes.unam.api.dto.vendedor.VendedorResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/vendedor")
@RequiredArgsConstructor
public class VendedorController {

    private final VendedorApiClient vendedorApiClient;
    private final BloqueApiClient bloqueApiClient;

    @GetMapping("/ver")
    public String ver(Model model) {
        List<VendedorResponse> vendedores = vendedorApiClient.listar();
        model.addAttribute("vendedores", vendedores);
        return "vendedor/vendedor";
    }

    @GetMapping("/alta")
    public String alta(Model model) {
        VendedorResponse vendedor = new VendedorResponse();
        vendedor.setActivo(true);
        model.addAttribute("vendedor", vendedor);
        model.addAttribute("bloques", bloqueApiClient.listarActivos());
        model.addAttribute("esEdicion", false);
        return "vendedor/alta-vendedor";
    }

    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Long id, Model model) {
        VendedorResponse vendedor = vendedorApiClient.obtener(id);
        model.addAttribute("vendedor", vendedor);
        model.addAttribute("esEdicion", true);
        // en edición el bloque es de solo lectura (inmutable)
        return "vendedor/alta-vendedor";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("vendedor") VendedorResponse vendedor,
                         BindingResult bindingResult,
                         @RequestParam(name = "password", required = false) String password,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        boolean esEdicion = vendedor != null && vendedor.getId() != null;
        String passwordNormalizada = StringUtils.hasText(password) ? password : null;

        VendedorRequest request = new VendedorRequest(
                vendedor != null ? vendedor.getNombre() : null,
                vendedor != null ? vendedor.getApellidos() : null,
                vendedor != null ? vendedor.getTelefono() : null,
                vendedor != null ? vendedor.getEmail() : null,
                vendedor != null ? vendedor.getUsername() : null,
                passwordNormalizada,
                esEdicion ? null : (vendedor != null ? vendedor.getBloqueId() : null)
        );

        try {
            if (esEdicion) {
                vendedorApiClient.actualizar(vendedor.getId(), request);
                redirectAttributes.addFlashAttribute("exito", "Vendedor actualizado correctamente.");
            } else {
                vendedorApiClient.crear(request);
                redirectAttributes.addFlashAttribute("exito", "Vendedor creado correctamente.");
            }
            return "redirect:/vendedor/ver";
        } catch (BackendApiException ex) {
            if (ex.getStatus() == 400) {
                aplicarErroresValidacion(ex, bindingResult, model);
                model.addAttribute("esEdicion", esEdicion);
                if (!esEdicion) {
                    model.addAttribute("bloques", bloqueApiClient.listarActivos());
                }
                return "vendedor/alta-vendedor";
            }
            throw ex;
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        vendedorApiClient.desactivar(id);
        redirectAttributes.addFlashAttribute("exito", "Vendedor desactivado correctamente.");
        return "redirect:/vendedor/ver";
    }

    @PostMapping("/reactivar/{id}")
    public String reactivar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        vendedorApiClient.reactivar(id);
        redirectAttributes.addFlashAttribute("exito", "Vendedor reactivado correctamente.");
        return "redirect:/vendedor/ver";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        vendedorApiClient.eliminar(id);
        redirectAttributes.addFlashAttribute("exito", "Vendedor eliminado correctamente.");
        return "redirect:/vendedor/ver";
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
                    if ("password".equals(field)) {
                        model.addAttribute("passwordError", msg);
                        continue;
                    }
                    if (field != null && bindingResult.getTarget() != null) {
                        try {
                            bindingResult.rejectValue(field, "backend", msg);
                        } catch (Exception ignored) {
                            // campo no presente en el objeto del formulario
                        }
                    }
                }
            }
        }
    }
}
