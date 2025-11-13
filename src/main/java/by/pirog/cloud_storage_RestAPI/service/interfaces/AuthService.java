package by.pirog.cloud_storage_RestAPI.service.interfaces;

import by.pirog.cloud_storage_RestAPI.dto.ResponseUserSignUpDTO;
import by.pirog.cloud_storage_RestAPI.storage.entity.UserEntity;

import java.util.Optional;

public interface  AuthService {
    ResponseUserSignUpDTO signUp(String username, String password);

    public Optional<UserEntity> findUserByUsername(String username);
}
