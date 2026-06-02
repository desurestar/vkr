package ru.zagrebin.server.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.zagrebin.server.data.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Optional<UserEntity> findByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);

    @Query("""
            select count(followed) > 0
            from UserEntity follower
            join follower.following followed
            where follower.id = :viewerId and followed.id = :userId
            """)
    boolean isFollowing(@Param("viewerId") Long viewerId, @Param("userId") Long userId);
}
