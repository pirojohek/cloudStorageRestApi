package by.pirog.cloud_storage_RestAPI.storage.repository;

import by.pirog.cloud_storage_RestAPI.storage.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

}
