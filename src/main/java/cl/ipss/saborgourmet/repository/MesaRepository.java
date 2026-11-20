package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.model.Mesa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
    List<Mesa> findByActivaTrueAndCapacidadGreaterThanEqualOrderByCapacidadAscNumeroAsc(int personas);
    List<Mesa> findAllByOrderByNumeroAsc();
    boolean existsByNumero(Integer numero);

    /** Bloquea la mesa hasta finalizar la transacción para impedir reservas simultáneas. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Mesa m where m.id = :id")
    Optional<Mesa> bloquearPorId(@Param("id") Long id);
}
