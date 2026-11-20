package cl.ipss.saborgourmet;

import cl.ipss.saborgourmet.model.EstadoReserva;
import cl.ipss.saborgourmet.model.Mesa;
import cl.ipss.saborgourmet.model.Reserva;
import cl.ipss.saborgourmet.repository.MesaRepository;
import cl.ipss.saborgourmet.service.ReservaService;
import cl.ipss.saborgourmet.web.ReservaForm;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import java.time.LocalDateTime;

/** Prueba de integración ejecutable sin servidor HTTP: crear, conflicto, modificar y cancelar. */
public class FlujoReservasManual {
    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(SaborGourmetApplication.class)
                .web(WebApplicationType.NONE)
                .properties("spring.datasource.url=jdbc:h2:mem:flujo;DB_CLOSE_DELAY=-1",
                        "spring.jpa.hibernate.ddl-auto=create-drop")
                .run(args)) {
            ReservaService servicio = ctx.getBean(ReservaService.class);
            Mesa mesa = ctx.getBean(MesaRepository.class).findAllByOrderByNumeroAsc().get(0);
            LocalDateTime inicio = LocalDateTime.now().plusDays(2).withHour(13).withMinute(0).withSecond(0).withNano(0);
            ReservaForm form = form(mesa, inicio);
            if (servicio.disponibles(inicio, 2, null).stream().noneMatch(m -> m.getId().equals(mesa.getId())))
                throw new AssertionError("La mesa debería estar libre");
            Reserva creada = servicio.crear(form);
            if (servicio.disponibles(inicio, 2, null).stream().anyMatch(m -> m.getId().equals(mesa.getId())))
                throw new AssertionError("La mesa no debería estar libre");
            try { servicio.crear(form); throw new AssertionError("Se permitió doble reserva"); }
            catch (IllegalArgumentException esperado) { /* Regla de conflicto comprobada. */ }
            form.setInicio(inicio.plusHours(3));
            servicio.modificar(creada.getId(), form);
            if (!servicio.buscar(creada.getId()).getInicio().equals(inicio.plusHours(3)))
                throw new AssertionError("La modificación no se persistió");
            servicio.cancelar(creada.getId());
            if (servicio.buscar(creada.getId()).getEstado() != EstadoReserva.CANCELADA)
                throw new AssertionError("La cancelación no se persistió");
            System.out.println("PRUEBA OK: disponibilidad, creación, conflicto, modificación y cancelación");
        }
    }

    private static ReservaForm form(Mesa mesa, LocalDateTime inicio) {
        ReservaForm f = new ReservaForm();
        f.setNombre("Cliente Prueba"); f.setEmail("prueba@example.com");
        f.setTelefono("+56912345678"); f.setMesaId(mesa.getId());
        f.setInicio(inicio); f.setPersonas(2);
        return f;
    }
}
