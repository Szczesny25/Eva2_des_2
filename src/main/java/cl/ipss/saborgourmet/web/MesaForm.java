package cl.ipss.saborgourmet.web;

import jakarta.validation.constraints.*;

public class MesaForm {
    @NotNull @Min(1) private Integer numero;
    @NotNull @Min(1) @Max(20) private Integer capacidad;
    private boolean activa = true;
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
