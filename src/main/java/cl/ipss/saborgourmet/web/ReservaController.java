package cl.ipss.saborgourmet.web;

import cl.ipss.saborgourmet.model.Reserva;
import cl.ipss.saborgourmet.repository.MesaRepository;
import cl.ipss.saborgourmet.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;

/** Controlador MVC para consulta, creación, edición y cancelación. */
@Controller
public class ReservaController {
    private final ReservaService servicio;
    private final MesaRepository mesas;
    public ReservaController(ReservaService servicio, MesaRepository mesas) {
        this.servicio = servicio; this.mesas = mesas;
    }

    @GetMapping("/")
    public String inicio() { return "inicio"; }

    /** Consulta de mesas libres para un intervalo de dos horas. */
    @GetMapping("/disponibilidad")
    public String disponibilidad(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime inicio,
                                 @RequestParam(required = false) Integer personas, Model model) {
        model.addAttribute("inicio", inicio);
        model.addAttribute("personas", personas);
        if (inicio != null && personas != null) {
            try { model.addAttribute("mesas", servicio.disponibles(inicio, personas, null)); }
            catch (IllegalArgumentException e) { model.addAttribute("error", e.getMessage()); }
        }
        return "disponibilidad";
    }

    @GetMapping("/reservas/nueva")
    public String nueva(@RequestParam(required = false) Long mesaId,
                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime inicio,
                        @RequestParam(required = false) Integer personas, Model model) {
        ReservaForm form = new ReservaForm();
        form.setMesaId(mesaId); form.setInicio(inicio); form.setPersonas(personas);
        model.addAttribute("reservaForm", form);
        model.addAttribute("titulo", "Nueva reserva");
        model.addAttribute("accion", "/reservas");
        model.addAttribute("mesas", mesas.findByActivaTrueAndCapacidadGreaterThanEqualOrderByCapacidadAscNumeroAsc(1));
        return "form-reserva";
    }

    /** Si falla la validación, vuelve a mostrar el formulario con los datos ingresados. */
    @PostMapping("/reservas")
    public String crear(@Valid @ModelAttribute("reservaForm") ReservaForm form, BindingResult result,
                        Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                Reserva reserva = servicio.crear(form);
                redirect.addFlashAttribute("exito", "Reserva #" + reserva.getId() + " confirmada");
                return "redirect:/mis-reservas?email=" + java.net.URLEncoder.encode(form.getEmail(), java.nio.charset.StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) { model.addAttribute("error", e.getMessage()); }
        }
        model.addAttribute("titulo", "Nueva reserva"); model.addAttribute("accion", "/reservas");
        model.addAttribute("mesas", mesas.findByActivaTrueAndCapacidadGreaterThanEqualOrderByCapacidadAscNumeroAsc(1));
        return "form-reserva";
    }

    @GetMapping("/mis-reservas")
    public String mias(@RequestParam(required = false) String email, Model model) {
        model.addAttribute("email", email);
        if (email != null && !email.isBlank()) model.addAttribute("reservas", servicio.porCorreo(email));
        return "mis-reservas";
    }

    @GetMapping("/reservas/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Reserva r = servicio.buscar(id);
        if (r.getEstado() == cl.ipss.saborgourmet.model.EstadoReserva.CANCELADA) return "redirect:/mis-reservas";
        ReservaForm form = new ReservaForm();
        form.setNombre(r.getCliente().getNombre()); form.setEmail(r.getCliente().getEmail());
        form.setTelefono(r.getCliente().getTelefono()); form.setMesaId(r.getMesa().getId());
        form.setInicio(r.getInicio()); form.setPersonas(r.getPersonas());
        model.addAttribute("reservaForm", form);
        model.addAttribute("titulo", "Modificar reserva #" + id);
        model.addAttribute("accion", "/reservas/" + id);
        model.addAttribute("mesas", mesas.findByActivaTrueAndCapacidadGreaterThanEqualOrderByCapacidadAscNumeroAsc(1));
        return "form-reserva";
    }

    @PostMapping("/reservas/{id}")
    public String modificar(@PathVariable Long id, @Valid @ModelAttribute("reservaForm") ReservaForm form,
                            BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                servicio.modificar(id, form);
                redirect.addFlashAttribute("exito", "Reserva modificada correctamente");
                return "redirect:/mis-reservas?email=" + java.net.URLEncoder.encode(form.getEmail(), java.nio.charset.StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) { model.addAttribute("error", e.getMessage()); }
        }
        model.addAttribute("titulo", "Modificar reserva #" + id);
        model.addAttribute("accion", "/reservas/" + id);
        model.addAttribute("mesas", mesas.findByActivaTrueAndCapacidadGreaterThanEqualOrderByCapacidadAscNumeroAsc(1));
        return "form-reserva";
    }

    /** POST evita cancelar una reserva por el simple hecho de abrir un enlace. */
    @PostMapping("/reservas/{id}/cancelar")
    public String cancelar(@PathVariable Long id, @RequestParam(required = false) String email,
                           RedirectAttributes redirect) {
        servicio.cancelar(id);
        redirect.addFlashAttribute("exito", "Reserva cancelada. La mesa vuelve a estar disponible.");
        return email == null || email.isBlank() ? "redirect:/admin" :
                "redirect:/mis-reservas?email=" + java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String error(IllegalArgumentException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error";
    }
}
