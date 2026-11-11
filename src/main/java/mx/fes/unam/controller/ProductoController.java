package mx.fes.unam.controller;

import jakarta.validation.Valid;
import mx.fes.unam.model.entities.producto.ProductoDTO;
import mx.fes.unam.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/producto")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/ver")
    public String ver(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        return "producto/producto";
    }

    @GetMapping("/alta")
    public String alta(Model model) {
        ProductoDTO dto = new ProductoDTO();
        model.addAttribute("producto", dto);
        model.addAttribute("esEdicion", false);
        return "producto/alta-producto";
    }

    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        ProductoDTO dto = productoService.buscarPorId(id);
        if (dto == null) {
            redirectAttributes.addFlashAttribute("error", "El producto no fue encontrado.");
            return "redirect:/producto/ver";
        }
        model.addAttribute("producto", dto);
        model.addAttribute("esEdicion", true);
        return "producto/alta-producto";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("producto") ProductoDTO productoDTO,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        boolean esEdicion = (productoDTO.getId() != null);

        if (bindingResult.hasErrors()) {
            model.addAttribute("esEdicion", esEdicion);
            return "producto/alta-producto";
        }

        try {
            productoService.guardar(productoDTO);
            redirectAttributes.addFlashAttribute("exito",
                    esEdicion ? "Producto actualizado exitosamente." : "Producto registrado exitosamente.");
            return "redirect:/producto/ver";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("esEdicion", esEdicion);
            return "producto/alta-producto";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Producto eliminado exitosamente.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al intentar eliminar el producto: " + e.getMessage());
        }
        return "redirect:/producto/ver";
    }
}
