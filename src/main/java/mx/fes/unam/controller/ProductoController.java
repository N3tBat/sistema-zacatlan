package mx.fes.unam.controller;

import lombok.RequiredArgsConstructor;
import mx.fes.unam.api.client.BackendApiException;
import mx.fes.unam.api.client.BloqueApiClient;
import mx.fes.unam.api.client.ProductoApiClient;
import mx.fes.unam.api.dto.error.ApiError;
import mx.fes.unam.api.dto.producto.ProductoRequest;
import mx.fes.unam.api.dto.producto.ProductoResponse;
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
@RequestMapping("/producto")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoApiClient productoApiClient;
    private final BloqueApiClient bloqueApiClient;

    @GetMapping("/ver")
    public String ver(Model model) {
        List<ProductoResponse> productos = productoApiClient.listar();
        model.addAttribute("productos", productos);
        return "producto/producto";
    }

    @GetMapping("/alta")
    public String alta(Model model) {
        ProductoResponse producto = new ProductoResponse();
        producto.setActivo(true);
        model.addAttribute("producto", producto);
        model.addAttribute("esEdicion", false);
        model.addAttribute("bloques", bloqueApiClient.listarActivos());
        return "producto/alta-producto";
    }

    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Long id, Model model) {
        ProductoResponse producto = productoApiClient.obtener(id);
        model.addAttribute("producto", producto);
        model.addAttribute("esEdicion", true);
        model.addAttribute("bloques", bloqueApiClient.listarActivos());
        return "producto/alta-producto";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("producto") ProductoResponse producto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        boolean esEdicion = producto != null && producto.getId() != null;

        ProductoRequest request = new ProductoRequest(
                producto != null ? producto.getNombre() : null,
                producto != null ? producto.getDescripcion() : null,
                producto != null ? producto.getFormaVenta() : null,
                producto != null ? producto.getPrecioUnidad() : null,
                producto != null ? producto.getPrecioBloque() : null,
                producto != null ? producto.getUnidadesPorBloque() : null,
                producto != null ? producto.getActivo() : null,
                producto != null ? producto.getBloqueId() : null
        );

        try {
            if (esEdicion) {
                productoApiClient.actualizar(producto.getId(), request);
                redirectAttributes.addFlashAttribute("exito", "Producto actualizado correctamente.");
            } else {
                productoApiClient.crear(request);
                redirectAttributes.addFlashAttribute("exito", "Producto creado correctamente.");
            }
            return "redirect:/producto/ver";
        } catch (BackendApiException ex) {
            if (ex.getStatus() == 400) {
                aplicarErroresValidacion(ex, bindingResult, model);
                model.addAttribute("esEdicion", esEdicion);
                model.addAttribute("bloques", bloqueApiClient.listarActivos());
                return "producto/alta-producto";
            }
            throw ex;
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        productoApiClient.eliminar(id);
        redirectAttributes.addFlashAttribute("exito", "Producto eliminado correctamente.");
        return "redirect:/producto/ver";
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
