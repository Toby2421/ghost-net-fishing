package de.ghostnet.repository;
import de.ghostnet.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByNameAndPhone(String name, String phone);
}
