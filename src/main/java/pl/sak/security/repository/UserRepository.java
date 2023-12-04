package pl.sak.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sak.security.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
