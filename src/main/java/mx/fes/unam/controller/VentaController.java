package mx.fes.unam.controller;

import jakarta.validation.Valid;
import mx.fes.unam.model.entities.venta.VentaDTO;
import mx.fes.unam.service.ProductoService;
import mx.fes.unam.service.VentaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/venta")
public class VentaController {

    private final VentaService ventaService;
    private final ProductoService productoService;

    public VentaController(VentaService ventaService, ProductoService productoService) {
        this.ventaService = ventaService;
        this.productoService = productoService;
    }

    // Lista: el ADMIN ve todas las ventas; el VENDEDOR solo las suyas
    @GetMapping("/ver")
    public String ver(Model model, Authentication authentication) {
        boolean esAdmin = esAdmin(authentication);
        model.addAttribute("ventas", esAdmin
                ? ventaService.listarTodos()
                : ventaService.listarPorVendedorUsername(authentication.getName()));
        model.addAttribute("esAdmin", esAdmin);
        return "venta/venta";
    }

    // Alta de venta: ADMIN elige vendedor; VENDEDOR queda fijo con su propia cuenta
    @GetMapping("/alta")
    public String alta(Model model, Authentication authentication) {
        boolean esAdmin = esAdmin(authentication);
        model.addAttribute("venta", new VentaDTO());
        model.addAttribute("productos", productoService.listarActivos());
        model.addAttribute("esAdmin", esAdmin);
        if (esAdmin) {
            model.addAttribute("vendedores", ventaService.listarVendedoresActivos());
        } else {
            model.addAttribute("vendedorPropio", ventaService.buscarVendedorPorUsername(authentication.getName()));
        }
        return "venta/alta-venta";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("venta") VentaDTO ventaDTO,
                         BindingResult bindingResult,
                         Model model,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        boolean esAdmin = esAdmin(authentication);

        // El VENDEDOR siempre registra a su nombre: el id del formulario se ignora
        if (!esAdmin) {
            var vendedorPropio = ventaService.buscarVendedorPorUsername(authentication.getName());
            ventaDTO.setVendedorId(vendedorPropio != null ? vendedorPropio.getId() : null);
        }

        if (bindingResult.hasErrors()) {
            return recargarFormularioAlta(model, esAdmin, authentication, ventaDTO);
        }

        try {
            ventaService.registrar(ventaDTO);
            redirectAttributes.addFlashAttribute("exito", "Venta registrada exitosamente.");
            return "redirect:/venta/ver";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return recargarFormularioAlta(model, esAdmin, authentication, ventaDTO);
        }
    }

    // Modificar venta: SOLO ADMIN (protegido también en SecurityConfig).
    // Aquí se llena la bitácora con el texto concatenado del cambio.
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Long id,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        VentaDTO venta = ventaService.buscarPorId(id);
        if (venta == null) {
            redirectAttributes.addFlashAttribute("error", "La venta no fue encontrada.");
            return "redirect:/venta/ver";
        }
        model.addAttribute("venta", venta);
        model.addAttribute("productos", productoService.listarActivos());
        return "venta/modificar-venta";
    }

    @PostMapping("/modificar/{id}")
    public String salvarModificacion(@PathVariable("id") Long id,
                                      @Valid @ModelAttribute("venta") VentaDTO ventaDTO,
                                      BindingResult bindingResult,
                                      Model model,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        VentaDTO actual = ventaService.buscarPorId(id);
        if (actual == null) {
            redirectAttributes.addFlashAttribute("error", "La venta no fue encontrada.");
            return "redirect:/venta/ver";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("venta", actual);
            model.addAttribute("productos", productoService.listarActivos());
            return "venta/modificar-venta";
        }

        try {
            ventaService.modificar(id, ventaDTO, authentication.getName());
            redirectAttributes.addFlashAttribute("exito", "Venta modificada exitosamente.");
            return "redirect:/venta/ver";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("venta", actual);
            model.addAttribute("productos", productoService.listarActivos());
            return "venta/modificar-venta";
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
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

    private String recargarFormularioAlta(Model model, boolean esAdmin, Authentication authentication, VentaDTO ventaDTO) {
        model.addAttribute("productos", productoService.listarActivos());
        model.addAttribute("esAdmin", esAdmin);
        if (esAdmin) {
            model.addAttribute("vendedores", ventaService.listarVendedoresActivos());
        } else {
            model.addAttribute("vendedorPropio", ventaService.buscarVendedorPorUsername(authentication.getName()));
        }
        return "venta/alta-venta";
    }
}
