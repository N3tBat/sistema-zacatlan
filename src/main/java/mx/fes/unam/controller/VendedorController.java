package mx.fes.unam.controller;

import jakarta.validation.Valid;
import mx.fes.unam.model.entities.vendedor.VendedorDTO;
import mx.fes.unam.security.RolEnum;
import mx.fes.unam.service.BloqueService;
import mx.fes.unam.service.VendedorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vendedor")
public class VendedorController {

    private final VendedorService vendedorService;
    private final BloqueService bloqueService;

    public VendedorController(VendedorService vendedorService, BloqueService bloqueService) {
        this.vendedorService = vendedorService;
        this.bloqueService = bloqueService;
    }

    @GetMapping("/ver")
    public String ver(Model model) {
        model.addAttribute("vendedores", vendedorService.listarVendedores());
        return "vendedor/vendedor";
    }

    @GetMapping("/alta")
    public String alta(Model model) {
        VendedorDTO dto = new VendedorDTO();
        dto.setActivo(true);
        model.addAttribute("vendedor", dto);
        model.addAttribute("bloques", bloqueService.listarActivos());
        model.addAttribute("esEdicion", false);
        return "vendedor/alta-vendedor";
    }

    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        VendedorDTO dto = vendedorService.buscarPorId(id);
        if (dto == null) {
            redirectAttributes.addFlashAttribute("error", "El vendedor no fue encontrado.");
            return "redirect:/vendedor/ver";
        }
        if (dto.getRol() == RolEnum.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "No se puede modificar un administrador desde este módulo.");
            return "redirect:/vendedor/ver";
        }

        model.addAttribute("vendedor", dto);
        model.addAttribute("bloques", bloqueService.listarActivos());
        model.addAttribute("esEdicion", true);
        return "vendedor/alta-vendedor";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("vendedor") VendedorDTO vendedorDTO,
                         BindingResult bindingResult,
                         @RequestParam(name = "password", required = false) String password,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        boolean esEdicion = (vendedorDTO.getId() != null);

        if (!esEdicion && (password == null || password.trim().isEmpty())) {
            model.addAttribute("passwordError", "La contraseña es obligatoria para nuevos vendedores.");
            bindingResult.reject("password.required", "La contraseña es obligatoria para nuevos vendedores.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("bloques", bloqueService.listarActivos());
            model.addAttribute("esEdicion", esEdicion);
            return "vendedor/alta-vendedor";
        }

        try {
            vendedorService.guardar(vendedorDTO, password);
            redirectAttributes.addFlashAttribute("exito",
                    esEdicion ? "Vendedor actualizado exitosamente." : "Vendedor registrado exitosamente.");
            return "redirect:/vendedor/ver";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("bloques", bloqueService.listarActivos());
            model.addAttribute("esEdicion", esEdicion);
            return "vendedor/alta-vendedor";
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            vendedorService.desactivar(id);
            redirectAttributes.addFlashAttribute("exito", "Vendedor desactivado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al desactivar el vendedor: " + e.getMessage());
        }
        return "redirect:/vendedor/ver";
    }

    @PostMapping("/reactivar/{id}")
    public String reactivar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            vendedorService.reactivar(id);
            redirectAttributes.addFlashAttribute("exito", "Vendedor reactivado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al reactivar el vendedor: " + e.getMessage());
        }
        return "redirect:/vendedor/ver";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            vendedorService.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Vendedor eliminado exitosamente. Sus ventas históricas fueron preservadas.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el vendedor: " + e.getMessage());
        }
        return "redirect:/vendedor/ver";
    }
}
