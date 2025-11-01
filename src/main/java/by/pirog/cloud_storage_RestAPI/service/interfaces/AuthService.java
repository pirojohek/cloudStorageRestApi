package by.pirog.cloud_storage_RestAPI.service.interfaces;

import by.pirog.cloud_storage_RestAPI.storage.entity.UserEntity;

import java.util.Optional;

public interface  AuthService {
    UserEntity signUp(String username, String password);

    public Optional<UserEntity> findUserByUsername(String username);
}
