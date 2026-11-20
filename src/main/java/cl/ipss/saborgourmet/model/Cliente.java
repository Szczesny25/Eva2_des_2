package cl.ipss.saborgourmet.model;

import jakarta.persistence.*;

/** Datos de contacto reutilizados en las reservas de un mismo correo. */
@Entity
public class Cliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String nombre;
    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String telefono;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
