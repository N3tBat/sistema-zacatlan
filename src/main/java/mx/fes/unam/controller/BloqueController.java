package mx.fes.unam.controller;

import jakarta.validation.Valid;
import mx.fes.unam.model.entities.bloque.BloqueDTO;
import mx.fes.unam.service.BloqueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bloque")
public class BloqueController {

    private final BloqueService bloqueService;

    public BloqueController(BloqueService bloqueService) {
        this.bloqueService = bloqueService;
    }

    @GetMapping("/ver")
    public String ver(Model model) {
        model.addAttribute("bloques", bloqueService.listarTodos());
        return "bloque/bloque";
    }

    @GetMapping("/alta")
    public String alta(Model model) {
        BloqueDTO bloqueDTO = new BloqueDTO();
        bloqueDTO.setActivo(true);
        model.addAttribute("bloque", bloqueDTO);
        model.addAttribute("esEdicion", false);
        return "bloque/alta-bloque";
    }

    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        BloqueDTO dto = bloqueService.buscarPorId(id);
        if (dto == null) {
            redirectAttributes.addFlashAttribute("error", "El bloque no fue encontrado.");
            return "redirect:/bloque/ver";
        }
        model.addAttribute("bloque", dto);
        model.addAttribute("esEdicion", true);
        return "bloque/alta-bloque";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("bloque") BloqueDTO bloqueDTO,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("esEdicion", bloqueDTO.getId() != null);
            return "bloque/alta-bloque";
        }

        bloqueService.guardar(bloqueDTO);
        redirectAttributes.addFlashAttribute("exito", "Bloque guardado exitosamente.");
        return "redirect:/bloque/ver";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            bloqueService.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Bloque eliminado exitosamente.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al intentar eliminar el bloque: " + e.getMessage());
        }
        return "redirect:/bloque/ver";
    }
}
