package cl.ipss.saborgourmet.model;

import jakarta.persistence.*;

/** Capacidad y estado de una mesa configurable por el personal. */
@Entity
public class Mesa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true) private Integer numero;
    @Column(nullable = false) private Integer capacidad;
    @Column(nullable = false) private boolean activa = true;

    public Long getId() { return id; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
