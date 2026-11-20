package cl.ipss.saborgourmet.web;

import cl.ipss.saborgourmet.model.Mesa;
import cl.ipss.saborgourmet.repository.MesaRepository;
import cl.ipss.saborgourmet.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Pantalla administrativa para consultar reservas y configurar mesas. */
@Controller
@RequestMapping("/admin")
public class AdminController {
    private final MesaRepository mesas;
    private final ReservaService reservas;
    public AdminController(MesaRepository mesas, ReservaService reservas) {
        this.mesas = mesas; this.reservas = reservas;
    }

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("reservas", reservas.todas());
        model.addAttribute("mesas", mesas.findAllByOrderByNumeroAsc());
        model.addAttribute("mesaForm", new MesaForm());
        return "admin";
    }

    @PostMapping("/mesas")
    public String crearMesa(@Valid @ModelAttribute MesaForm form, BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) redirect.addFlashAttribute("error", "Revisa número y capacidad de la mesa");
        else if (mesas.existsByNumero(form.getNumero())) redirect.addFlashAttribute("error", "Ese número de mesa ya existe");
        else {
            Mesa mesa = new Mesa(); mesa.setNumero(form.getNumero());
            mesa.setCapacidad(form.getCapacidad()); mesa.setActiva(form.isActiva());
            mesas.save(mesa); redirect.addFlashAttribute("exito", "Mesa agregada");
        }
        return "redirect:/admin";
    }

    @PostMapping("/mesas/{id}")
    public String actualizarMesa(@PathVariable Long id, @RequestParam int capacidad,
                                 @RequestParam(defaultValue = "false") boolean activa,
                                 RedirectAttributes redirect) {
        Mesa mesa = mesas.findById(id).orElseThrow(() -> new IllegalArgumentException("Mesa inexistente"));
        if (capacidad < 1 || capacidad > 20) redirect.addFlashAttribute("error", "Capacidad entre 1 y 20");
        else {
            mesa.setCapacidad(capacidad); mesa.setActiva(activa); mesas.save(mesa);
            redirect.addFlashAttribute("exito", "Mesa actualizada");
        }
        return "redirect:/admin";
    }
}
