package de.ghostnet.repository;
import de.ghostnet.model.GhostNet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface GhostNetRepository extends JpaRepository<GhostNet, Long> {
    // Sperrt den Datensatz bis zum Transaktionsende gegen konkurrierende Übernahmen.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from GhostNet n where n.id = :id")
    Optional<GhostNet> findForUpdate(@Param("id") Long id);
}
