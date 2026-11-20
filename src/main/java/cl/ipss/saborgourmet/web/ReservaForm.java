package cl.ipss.saborgourmet.web;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

/** Datos ingresados en la vista; la entidad no se enlaza directamente al formulario. */
public class ReservaForm {
    @NotBlank(message = "Ingresa tu nombre") private String nombre;
    @NotBlank(message = "Ingresa tu correo") @Email(message = "Correo inválido") private String email;
    @NotBlank(message = "Ingresa tu teléfono") private String telefono;
    @NotNull(message = "Elige una fecha y hora") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") private LocalDateTime inicio;
    @NotNull(message = "Indica la cantidad de personas") @Min(value = 1, message = "Mínimo una persona") @Max(value = 20, message = "Máximo 20 personas") private Integer personas;
    @NotNull(message = "Elige una mesa") private Long mesaId;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }
    public Integer getPersonas() { return personas; }
    public void setPersonas(Integer personas) { this.personas = personas; }
    public Long getMesaId() { return mesaId; }
    public void setMesaId(Long mesaId) { this.mesaId = mesaId; }
}
