package mx.fes.unam.controller;

import lombok.RequiredArgsConstructor;
import mx.fes.unam.api.client.AuthApiClient;
import mx.fes.unam.api.client.BackendApiException;
import mx.fes.unam.api.client.ProductoApiClient;
import mx.fes.unam.api.client.VendedorApiClient;
import mx.fes.unam.api.client.VentaApiClient;
import mx.fes.unam.api.dto.auth.MeResponse;
import mx.fes.unam.api.dto.error.ApiError;
import mx.fes.unam.api.dto.vendedor.VendedorResponse;
import mx.fes.unam.api.dto.venta.VentaRequest;
import mx.fes.unam.api.dto.venta.VentaResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/venta")
@RequiredArgsConstructor
public class VentaController {

    private final VentaApiClient ventaApiClient;
    private final ProductoApiClient productoApiClient;
    private final VendedorApiClient vendedorApiClient;
    private final AuthApiClient authApiClient;

    @GetMapping("/ver")
    public String ver(Model model, Authentication authentication) {
        boolean esAdmin = esAdmin(authentication);
        model.addAttribute("ventas", ventaApiClient.listar());
        model.addAttribute("esAdmin", esAdmin);
        return "venta/venta";
    }

    @GetMapping("/alta")
    public String alta(Model model, Authentication authentication) {
        boolean esAdmin = esAdmin(authentication);
        VentaResponse venta = new VentaResponse();
        model.addAttribute("venta", venta);
        model.addAttribute("productos", productoApiClient.listarActivos());
        model.addAttribute("esAdmin", esAdmin);
        cargarDatosAltaPorRol(model, esAdmin);
        return "venta/alta-venta";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("venta") VentaResponse venta,
                         BindingResult bindingResult,
                         Model model,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        boolean esAdmin = esAdmin(authentication);

        VentaRequest request = new VentaRequest(
                venta != null ? venta.getCantidad() : null,
                venta != null ? venta.getPrecioAplicado() : null,
                venta != null ? venta.getProductoId() : null,
                esAdmin ? (venta != null ? venta.getVendedorId() : null) : null
        );

        try {
            ventaApiClient.crear(request);
            redirectAttributes.addFlashAttribute("exito", "Venta registrada correctamente.");
            return "redirect:/venta/ver";
        } catch (BackendApiException ex) {
            if (ex.getStatus() == 400 || ex.getStatus() == 409) {
                aplicarErroresValidacion(ex, bindingResult, model);
                model.addAttribute("productos", productoApiClient.listarActivos());
                model.addAttribute("esAdmin", esAdmin);
                cargarDatosAltaPorRol(model, esAdmin);
                return "venta/alta-venta";
            }
            throw ex;
        }
    }

    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Long id, Model model) {
        VentaResponse venta = ventaApiClient.obtener(id);
        model.addAttribute("venta", venta);
        model.addAttribute("productos", productoApiClient.listarActivos());
        return "venta/modificar-venta";
    }

    @PostMapping("/modificar/{id}")
    public String salvarModificacion(@PathVariable("id") Long id,
                                     @ModelAttribute("venta") VentaResponse venta,
                                     BindingResult bindingResult,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        // No se permite cambio de vendedor: se preserva el vendedor original de la venta.
        VentaResponse original = ventaApiClient.obtener(id);

        VentaRequest request = new VentaRequest(
                venta != null ? venta.getCantidad() : null,
                venta != null ? venta.getPrecioAplicado() : null,
                venta != null ? venta.getProductoId() : null,
                original != null ? original.getVendedorId() : null
        );

        try {
            ventaApiClient.actualizar(id, request);
            redirectAttributes.addFlashAttribute("exito", "Venta modificada correctamente.");
            return "redirect:/venta/ver";
        } catch (BackendApiException ex) {
            if (ex.getStatus() == 400 || ex.getStatus() == 409) {
                aplicarErroresValidacion(ex, bindingResult, model);
                // Mantener datos de visualización originales (vendedor/fecha) y cambios editados
                if (original != null && venta != null) {
                    venta.setVendedorId(original.getVendedorId());
                    venta.setVendedorNombre(original.getVendedorNombre());
                    venta.setFecha(original.getFecha());
                    venta.setId(original.getId());
                }
                model.addAttribute("venta", venta);
                model.addAttribute("productos", productoApiClient.listarActivos());
                return "venta/modificar-venta";
            }
            throw ex;
        }
    }

    private void cargarDatosAltaPorRol(Model model, boolean esAdmin) {
        if (esAdmin) {
            List<VendedorResponse> vendedoresActivos = vendedorApiClient.listar().stream()
                    .filter(v -> Boolean.TRUE.equals(v.getActivo()))
                    .collect(Collectors.toList());
            model.addAttribute("vendedores", vendedoresActivos);
        } else {
            MeResponse perfil = authApiClient.me();
            model.addAttribute("vendedorPropio", perfil);
        }
    }

    private boolean esAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
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
                            // campo no presente en el objeto del formulario
                        }
                    }
                }
            }
        }
    }
}
