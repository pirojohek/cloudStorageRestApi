package by.pirog.cloud_storage_RestAPI.service.defaultSerivces;

import by.pirog.cloud_storage_RestAPI.exception.customExceptions.UsernameAlreadyExistsException;
import by.pirog.cloud_storage_RestAPI.service.interfaces.AuthService;
import by.pirog.cloud_storage_RestAPI.storage.entity.UserEntity;
import by.pirog.cloud_storage_RestAPI.storage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultAuthService implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserEntity signUp(String username, String password){
        if (userRepository.existsByUsername(username)){
            throw new UsernameAlreadyExistsException("Username %s is already exists");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(passwordEncoder.encode(password));
        userRepository.save(userEntity);
        return userEntity;
    }

    public Optional<UserEntity> findUserByUsername(String username){
        return userRepository.findByUsername(username);
    }


}
