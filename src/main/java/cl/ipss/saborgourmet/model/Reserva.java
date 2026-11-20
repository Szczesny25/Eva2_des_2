package cl.ipss.saborgourmet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Reserva de dos horas; el intervalo [inicio, fin) evita ambigüedad en los límites. */
@Entity
public class Reserva {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) private Cliente cliente;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) private Mesa mesa;
    @Column(nullable = false) private LocalDateTime inicio;
    @Column(nullable = false) private LocalDateTime fin;
    @Column(nullable = false) private Integer personas;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private EstadoReserva estado = EstadoReserva.ACTIVA;

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Mesa getMesa() { return mesa; }
    public void setMesa(Mesa mesa) { this.mesa = mesa; }
    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }
    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) { this.fin = fin; }
    public Integer getPersonas() { return personas; }
    public void setPersonas(Integer personas) { this.personas = personas; }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
}
