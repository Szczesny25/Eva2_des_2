package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.model.EstadoReserva;
import cl.ipss.saborgourmet.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    /** Hay conflicto cuando los intervalos se superponen estrictamente. */
    @Query("select count(r) from Reserva r where r.mesa.id = :mesaId and r.estado = :estado and r.inicio < :fin and r.fin > :inicio and (:excluirId is null or r.id <> :excluirId)")
    long contarConflictos(@Param("mesaId") Long mesaId, @Param("estado") EstadoReserva estado,
                          @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin,
                          @Param("excluirId") Long excluirId);

    List<Reserva> findByClienteEmailIgnoreCaseOrderByInicioDesc(String email);
    List<Reserva> findAllByOrderByInicioDesc();
}
