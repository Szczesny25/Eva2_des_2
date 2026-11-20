package cl.ipss.saborgourmet.service;

import cl.ipss.saborgourmet.model.*;
import cl.ipss.saborgourmet.repository.*;
import cl.ipss.saborgourmet.web.ReservaForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Reglas de negocio de disponibilidad y ciclo de vida de las reservas. */
@Service
public class ReservaService {
    private final ReservaRepository reservas;
    private final MesaRepository mesas;
    private final ClienteRepository clientes;

    public ReservaService(ReservaRepository reservas, MesaRepository mesas, ClienteRepository clientes) {
        this.reservas = reservas; this.mesas = mesas; this.clientes = clientes;
    }

    /** Muestra solo mesas capaces y libres; la confirmación vuelve a comprobarlas. */
    @Transactional(readOnly = true)
    public List<Mesa> disponibles(LocalDateTime inicio, int personas, Long excluirId) {
        validarHorario(inicio);
        if (personas < 1 || personas > 20) throw new IllegalArgumentException("Cantidad de personas inválida");
        return mesas.findByActivaTrueAndCapacidadGreaterThanEqualOrderByCapacidadAscNumeroAsc(personas)
                .stream().filter(m -> reservas.contarConflictos(m.getId(), EstadoReserva.ACTIVA, inicio,
                        inicio.plusHours(2), excluirId) == 0).toList();
    }

    @Transactional
    public Reserva crear(ReservaForm form) {
        return guardar(null, form);
    }

    /** Reutiliza la misma validación que la creación, excluyendo la reserva actual. */
    @Transactional
    public Reserva modificar(Long id, ReservaForm form) {
        return guardar(id, form);
    }

    /**
     * Bloquea la fila de la mesa antes de consultar conflictos. Así, dos transacciones
     * que intenten reservar la misma mesa se ejecutan en orden y no crean un duplicado.
     */
    private Reserva guardar(Long id, ReservaForm form) {
        validarHorario(form.getInicio());
        if (form.getPersonas() == null || form.getPersonas() < 1 || form.getPersonas() > 20)
            throw new IllegalArgumentException("Cantidad de personas inválida");
        Mesa mesa = mesas.bloquearPorId(form.getMesaId())
                .orElseThrow(() -> new IllegalArgumentException("La mesa no existe"));
        if (!mesa.isActiva() || mesa.getCapacidad() < form.getPersonas())
            throw new IllegalArgumentException("La mesa no está activa o no tiene capacidad suficiente");
        if (reservas.contarConflictos(mesa.getId(), EstadoReserva.ACTIVA, form.getInicio(),
                form.getInicio().plusHours(2), id) > 0)
            throw new IllegalArgumentException("La mesa ya está reservada en ese horario");
        Reserva reserva = id == null ? new Reserva() : reservas.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La reserva no existe"));
        if (reserva.getEstado() == EstadoReserva.CANCELADA)
            throw new IllegalArgumentException("No se puede modificar una reserva cancelada");
        String email = form.getEmail().trim().toLowerCase();
        Cliente cliente = clientes.findByEmailIgnoreCase(email).orElseGet(Cliente::new);
        cliente.setNombre(form.getNombre().trim());
        cliente.setEmail(email);
        cliente.setTelefono(form.getTelefono().trim());
        reserva.setCliente(clientes.save(cliente));
        reserva.setMesa(mesa);
        reserva.setInicio(form.getInicio());
        reserva.setFin(form.getInicio().plusHours(2));
        reserva.setPersonas(form.getPersonas());
        return reservas.save(reserva);
    }

    @Transactional
    public void cancelar(Long id) {
        Reserva reserva = reservas.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La reserva no existe"));
        reserva.setEstado(EstadoReserva.CANCELADA);
    }

    @Transactional(readOnly = true)
    public Reserva buscar(Long id) {
        return reservas.findById(id).orElseThrow(() -> new IllegalArgumentException("La reserva no existe"));
    }

    @Transactional(readOnly = true)
    public List<Reserva> porCorreo(String email) {
        return reservas.findByClienteEmailIgnoreCaseOrderByInicioDesc(email.trim());
    }

    @Transactional(readOnly = true)
    public List<Reserva> todas() { return reservas.findAllByOrderByInicioDesc(); }

    private void validarHorario(LocalDateTime inicio) {
        if (inicio == null || !inicio.isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Elige una fecha y hora futura");
        LocalTime hora = inicio.toLocalTime();
        if (hora.isBefore(LocalTime.NOON) || hora.isAfter(LocalTime.of(20, 0)))
            throw new IllegalArgumentException("Reservas disponibles entre las 12:00 y las 20:00");
    }
}
