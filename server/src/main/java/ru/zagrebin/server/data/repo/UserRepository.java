package ru.zagrebin.server.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zagrebin.server.data.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Optional<UserEntity> findByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
}
